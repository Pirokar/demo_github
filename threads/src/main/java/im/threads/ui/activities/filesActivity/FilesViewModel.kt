package im.threads.ui.activities.filesActivity

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import im.threads.business.annotation.OpenForTesting
import im.threads.business.broadcastReceivers.ProgressReceiver
import im.threads.business.logger.LoggerEdna
import im.threads.business.models.FileDescription
import im.threads.business.secureDatabase.DatabaseHolder
import im.threads.business.utils.FileUtils.getMimeType
import im.threads.business.utils.FileUtils.isImage
import im.threads.business.workers.FileDownloadWorker.Companion.startDownloadFD
import im.threads.ui.activities.ImagesActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * ViewModel для экрана со списком файлов
 * @param context applicationContext
 * @param database ссылка на базу данных
 */
@OpenForTesting
internal class FilesViewModel(
    private val context: Context,
    private val database: DatabaseHolder
) : ViewModel(), ProgressReceiver.Callback {

    class Factory(
        private val context: Context,
        private val database: DatabaseHolder
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return FilesViewModel(context, database) as T
        }
    }

    private val compositeDisposable = CompositeDisposable()

    /**
     * Приватная liveData. Не должна использоваться за пределами класса.
     * Открыта исключительно для тестирования.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val localIntentLiveData = MutableLiveData<Intent>()

    /**
     * LiveData, описывающая поток интентов.
     */
    val intentLiveData: LiveData<Intent> get() = localIntentLiveData

    /**
     * Приватная liveData. Не должна использоваться за пределами класса.
     * Открыта исключительно для тестирования.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val localFilesFlowLiveData = MutableLiveData<FilesFlow>()

    /**
     * LiveData, описывающая поток событий для файлов.
     */
    val filesFlowLiveData: LiveData<FilesFlow> get() = localFilesFlowLiveData

    // Для приема сообщений из сервиса по скачиванию файлов
    private val progressReceiver = ProgressReceiver(this)

    init {
        connectReceiver()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public override fun onCleared() {
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

    /**
     * Запрашивает файлы из базы данных асинхронно
     */
    fun getFilesAsync() {
        compositeDisposable.add(
            database.allFileDescriptions
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::onFilesReceived, ::onFilesReceivedError)
        )
    }

    /**
     * Реагирует на нажатие на файл.
     * Разделяет создание интента на картинку и остальные случаи.
     * Передает интент для запуска во view.
     * @param fileDescription описание файла
     */
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

    /**
     * Запускает загрузку файла, если url к нему не пустой.
     * @param fileDescription описание файла
     */
    fun onDownloadFileClick(fileDescription: FileDescription?) {
        val downloadPath = fileDescription?.fileUri?.toString() ?: fileDescription?.downloadPath
        downloadPath?.let { startDownloadFD(context, fileDescription!!) }
    }

    private fun onFilesReceived(list: List<FileDescription?>?) {
        localFilesFlowLiveData.value = FilesFlow.FilesReceived(list)
    }

    private fun onFilesReceivedError(error: Throwable) {
        LoggerEdna.error("getAllFileDescriptions error: ${error.message}")
    }

    private fun connectReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ProgressReceiver.PROGRESS_BROADCAST)
        intentFilter.addAction(ProgressReceiver.DOWNLOADED_SUCCESSFULLY_BROADCAST)
        intentFilter.addAction(ProgressReceiver.DOWNLOAD_ERROR_BROADCAST)
        LocalBroadcastManager.getInstance(context).registerReceiver(progressReceiver, intentFilter)
    }

    /**
     * Это приватный метод. Открыт только в целях тестирования.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun disconnectReceiver() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(progressReceiver)
    }
}
