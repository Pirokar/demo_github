package im.threads.business.models

data class FileAndMediaItem(val fileDescription: FileDescription, val fileName: String) :
    MediaAndFileItem {
    override val timeStamp: Long
        get() = fileDescription.timeStamp
}
