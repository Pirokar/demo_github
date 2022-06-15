package im.threads.internal.activities.files_activity

import im.threads.internal.model.FileDescription

sealed class FilesFlow {
    class UpdatedProgress(val fileDescription: FileDescription?) : FilesFlow()
    class DownloadError(val fileDescription: FileDescription?, val throwable: Throwable?) : FilesFlow()
    class FilesReceived(val files: List<FileDescription?>?) : FilesFlow()
}
