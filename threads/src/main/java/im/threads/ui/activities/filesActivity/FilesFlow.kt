package im.threads.ui.activities.filesActivity

import im.threads.business.models.FileDescription

/**
 * Описывает состояние загрузки файлов
 */
internal sealed class FilesFlow {
    class UpdatedProgress(val fileDescription: FileDescription?) : FilesFlow()
    class DownloadError(val fileDescription: FileDescription?, val throwable: Throwable?) : FilesFlow()
    class FilesReceived(val files: List<FileDescription?>?) : FilesFlow()
}
