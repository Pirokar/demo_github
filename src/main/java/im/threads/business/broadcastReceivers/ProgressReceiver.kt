package im.threads.business.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import im.threads.business.logger.LoggerEdna
import im.threads.business.models.FileDescription
import im.threads.business.workers.FileDownloadWorker
import java.lang.ref.SoftReference

/**
 * В чате есть возможность скачать файл из сообщения.
 * Он скачивается через сервис.
 * Для приема сообщений из сервиса используется данный BroadcastReceiver
 */
class ProgressReceiver(callback: Callback) : BroadcastReceiver() {
    private val callback = SoftReference(callback)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        when (action) {
            PROGRESS_BROADCAST -> {
                intent.getParcelableExtra<FileDescription>(FileDownloadWorker.FD_TAG)?.let {
                    callback.get()?.updateProgress(it)
                }
            }
            DOWNLOADED_SUCCESSFULLY_BROADCAST -> {
                LoggerEdna.info("onReceive: DOWNLOADED_SUCCESSFULLY_BROADCAST ")
                intent.getParcelableExtra<FileDescription>(FileDownloadWorker.FD_TAG)?.let {
                    it.downloadProgress = 100
                    callback.get()?.updateProgress(it)
                }
            }
            DOWNLOAD_ERROR_BROADCAST -> {
                LoggerEdna.error("onReceive: DOWNLOAD_ERROR_BROADCAST ")
                intent.getParcelableExtra<FileDescription>(FileDownloadWorker.FD_TAG)?.let {
                    callback.get()?.onDownloadError(
                        it,
                        intent.getSerializableExtra(DOWNLOAD_ERROR_BROADCAST) as Throwable?
                    )
                }
            }
        }
    }

    interface Callback {
        /**
         * Определяет реакцию на обновление прогресса
         * @param fileDescription характеристики файла, где произошел прогресс загрузки
         */
        fun updateProgress(fileDescription: FileDescription?)

        /**
         * Определяет реакцию на ошибку при загрузке
         * @param fileDescription характеристики файла, где произошла ошибка загрузки
         */
        fun onDownloadError(fileDescription: FileDescription?, throwable: Throwable?)
    }

    companion object {
        // Сообщения для Broadcast Receivers
        const val PROGRESS_BROADCAST = "im.threads.internal.controllers.PROGRESS_BROADCAST"
        const val DOWNLOADED_SUCCESSFULLY_BROADCAST =
            "im.threads.internal.controllers.DOWNLOADED_SUCCESSFULLY_BROADCAST"
        const val DOWNLOAD_ERROR_BROADCAST =
            "im.threads.internal.controllers.DOWNLOAD_ERROR_BROADCAST"
    }
}
