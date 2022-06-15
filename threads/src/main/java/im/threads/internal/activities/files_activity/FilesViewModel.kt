package im.threads.internal.activities.files_activity

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import im.threads.internal.activities.ImagesActivity
import im.threads.internal.broadcastReceivers.ProgressReceiver
import im.threads.internal.model.FileDescription
import im.threads.internal.secureDatabase.DatabaseHolder
import im.threads.internal.utils.FileUtils.getMimeType
import im.threads.internal.utils.FileUtils.isImage
import im.threads.internal.utils.ThreadsLogger
import im.threads.internal.workers.FileDownloadWorker.Companion.startDownloadFD
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class FilesViewModel(
    private val context: Context,
    private val database: DatabaseHolder
) : ViewModel(), ProgressReceiver.Callback {

    class Factory(
        private val context: Context,
        private val database: DatabaseHolder
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return FilesViewModel(context, database) as T
        }
    }

    private val tag = FilesViewModel::class.java.canonicalName
    private val compositeDisposable = CompositeDisposable()

    private val localIntentLiveData = MutableLiveData<Intent>()
    val intentLiveData: LiveData<Intent> get() = localIntentLiveData
    private val localFilesFlowLiveData = MutableLiveData<FilesFlow>()
    val filesFlowLiveData: LiveData<FilesFlow> get() = localFilesFlowLiveData

    // Для приема сообщений из сервиса по скачиванию файлов
    private val progressReceiver = ProgressReceiver(this)

    init {
        connectReceiver()
    }

    override fun onCleared() {
        super.onCleared()
        disconnectReceiver()
        compositeDisposable.dispose()
    }

    override fun updateProgress(fileDescription: FileDescription?) {
        localFilesFlowLiveData.value = FilesFlow.UpdatedProgress(fileDescription)
    }

    override fun onDownloadError(fileDescription: FileDescription?, throwable: Throwable?) {
        localFilesFlowLiveData.value = FilesFlow.DownloadError(fileDescription, throwable)
    }

    fun getFilesAsync() {
        compositeDisposable.add(
            database.allFileDescriptions
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::onFilesReceived, ::onFilesReceivedError)
        )
    }

    fun onFileClick(fileDescription: FileDescription?) {
        if (fileDescription?.fileUri == null) {
            return
        }
        if (isImage(fileDescription)) {
            localIntentLiveData.value = ImagesActivity.getStartIntent(context, fileDescription)
        } else {
            val target = Intent(Intent.ACTION_VIEW)
            target.setDataAndType(fileDescription.fileUri, getMimeType(fileDescription))
            target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
            localIntentLiveData.value = target
        }
    }

    fun onDownloadFileClick(fileDescription: FileDescription?) {
        val downloadPath = fileDescription?.fileUri?.toString() ?: fileDescription?.downloadPath
        downloadPath?.let { startDownloadFD(context, fileDescription!!) }
    }

    private fun onFilesReceived(list: List<FileDescription?>?) {
        localFilesFlowLiveData.value = FilesFlow.FilesReceived(list)
    }

    private fun onFilesReceivedError(error: Throwable) {
        ThreadsLogger.e(tag, "getAllFileDescriptions error: ${error.message}")
    }

    private fun connectReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ProgressReceiver.PROGRESS_BROADCAST)
        intentFilter.addAction(ProgressReceiver.DOWNLOADED_SUCCESSFULLY_BROADCAST)
        intentFilter.addAction(ProgressReceiver.DOWNLOAD_ERROR_BROADCAST)
        LocalBroadcastManager.getInstance(context).registerReceiver(progressReceiver, intentFilter)
    }

    private fun disconnectReceiver() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(progressReceiver)
    }
}
