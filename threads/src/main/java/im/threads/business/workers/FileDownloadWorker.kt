package im.threads.business.workers

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import im.threads.business.broadcastReceivers.ProgressReceiver
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna
import im.threads.business.models.FileDescription
import im.threads.business.models.FileDescriptionUri
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.preferences.Preferences
import im.threads.business.secureDatabase.DatabaseHolder
import im.threads.business.serviceLocator.core.inject
import im.threads.business.utils.FileDownloader
import im.threads.business.utils.FileDownloader.DownloadListener
import im.threads.business.utils.FileProviderHelper
import im.threads.business.utils.WorkerUtils.marshall
import im.threads.business.utils.WorkerUtils.unmarshall
import java.io.File

class FileDownloadWorker(val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    private var runningDownloads = HashMap<FileDescription, FileDownloader>()
    private val preferences: Preferences by inject()

    override fun doWork(): Result {
        val data = inputData.getByteArray(FD_TAG)?.let { unmarshall(it) }
        val fileDescription: FileDescription = FileDescription.CREATOR.createFromParcel(data)
            ?: return Result.failure()

        if (fileDescription.downloadPath == null || fileDescription.fileUri != null) {
            LoggerEdna.error("cant download with fileDescription = $fileDescription")
            return Result.failure()
        }

        if (fileDescription.state != AttachmentStateEnum.READY) {
            LoggerEdna.error("cant download with fileDescription = $fileDescription. File state not READY")
            return Result.failure()
        }

        if (runningDownloads.containsKey(fileDescription)) {
            return Result.failure()
        }

        val fileDownloader = FileDownloader(
            fileDescription.downloadPath!!,
            fileDescription.incomingName,
            context,
            object : DownloadListener {
                override fun onProgress(progress: Double) {
                    var downloadProgress = progress
                    if (downloadProgress < 1) downloadProgress = 1.0
                    fileDescription.downloadProgress = downloadProgress.toInt()
                    DatabaseHolder.getInstance().updateFileDescription(fileDescription)
                    sendDownloadProgressBroadcast(fileDescription)
                }

                override fun onComplete(file: File) {
                    fileDescription.downloadProgress = 100
                    val fileUri = FileProviderHelper.getUriForFile(
                        BaseConfig.instance.context,
                        file
                    )
                    fileDescription.fileUri = fileUri
                    DatabaseHolder.getInstance().updateFileDescription(fileDescription)
                    runningDownloads.remove(fileDescription)
                    sendFinishBroadcast(fileDescription)
                    fileDescription.onCompleteSubject.onNext(
                        FileDescriptionUri(fileDescription.downloadPath ?: "", fileUri)
                    )
                }

                override fun onFileDownloadError(e: Exception?) {
                    LoggerEdna.error("error while downloading file ", e)
                    fileDescription.downloadProgress = 0
                    DatabaseHolder.getInstance().updateFileDescription(fileDescription)
                    e?.let { sendDownloadErrorBroadcast(fileDescription, e) }
                }
            },
            preferences
        )

        if (START_DOWNLOAD_FD_TAG == inputData.getString(START_DOWNLOAD_ACTION)) {
            if (runningDownloads.containsKey(fileDescription)) {
                val downloader = runningDownloads[fileDescription]
                runningDownloads.remove(fileDescription)
                downloader?.stop()
                fileDescription.downloadProgress = 0
                sendDownloadProgressBroadcast(fileDescription)
                DatabaseHolder.getInstance().updateFileDescription(fileDescription)
            } else {
                runningDownloads[fileDescription] = fileDownloader
                fileDescription.downloadProgress = 1
                sendDownloadProgressBroadcast(fileDescription)
                runningDownloads[fileDescription] = fileDownloader
                fileDownloader.download()
            }
        } else if (START_DOWNLOAD_WITH_NO_STOP == inputData.getString(START_DOWNLOAD_ACTION)) {
            if (!runningDownloads.containsKey(fileDescription)) {
                runningDownloads[fileDescription] = fileDownloader
                fileDescription.downloadProgress = 1
                sendDownloadProgressBroadcast(fileDescription)
                runningDownloads[fileDescription] = fileDownloader
                fileDownloader.download()
            }
        }
        return Result.success()
    }

    private fun sendDownloadProgressBroadcast(fileDescription: FileDescription) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(
            Intent(ProgressReceiver.PROGRESS_BROADCAST).putExtra(FD_TAG, fileDescription)
        )
    }

    private fun sendFinishBroadcast(fileDescription: FileDescription) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(
            Intent(ProgressReceiver.DOWNLOADED_SUCCESSFULLY_BROADCAST).putExtra(
                FD_TAG,
                fileDescription
            )
        )
    }

    private fun sendDownloadErrorBroadcast(fileDescription: FileDescription, throwable: Throwable) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(
            Intent(ProgressReceiver.DOWNLOAD_ERROR_BROADCAST)
                .putExtra(FD_TAG, fileDescription)
                .putExtra(ProgressReceiver.DOWNLOAD_ERROR_BROADCAST, throwable)
        )
    }

    // TODO: remove static after ChatController
    companion object {
        private const val WORKER_NAME = "im.threads.business.workers.FileDownloadWorker"

        const val FD_TAG = "im.threads.business.workers.FileDownloadWorker.FD_TAG"
        const val START_DOWNLOAD_ACTION = "im.threads.business.workers.FileDownloadWorker.Action"
        const val START_DOWNLOAD_FD_TAG =
            "im.threads.business.workers.FileDownloadWorker.START_DOWNLOAD_FD_TAG"
        const val START_DOWNLOAD_WITH_NO_STOP =
            "im.threads.business.workers.FileDownloadWorker.START_DOWNLOAD_WITH_NO_STOP"

        @JvmStatic
        fun startDownloadFD(context: Context, fileDescription: FileDescription) {
            LoggerEdna.debug("Start download")
            val inputData = Data.Builder()
                .putString(START_DOWNLOAD_ACTION, START_DOWNLOAD_FD_TAG)
                .putByteArray(FD_TAG, marshall(fileDescription))
            val workRequest = OneTimeWorkRequestBuilder<FileDownloadWorker>()
                .setInputData(inputData.build())
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORKER_NAME + fileDescription.downloadPath, ExistingWorkPolicy.KEEP, workRequest)
        }

        @JvmStatic
        fun startDownloadWithNoStop(context: Context, fileDescription: FileDescription) {
            LoggerEdna.debug("Start download")
            val inputData = Data.Builder()
                .putString(START_DOWNLOAD_ACTION, START_DOWNLOAD_WITH_NO_STOP)
                .putByteArray(FD_TAG, marshall(fileDescription))
            val workRequest = OneTimeWorkRequestBuilder<FileDownloadWorker>()
                .setInputData(inputData.build())
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORKER_NAME + fileDescription.downloadPath, ExistingWorkPolicy.KEEP, workRequest)
        }
    }
}
