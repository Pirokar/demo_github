package im.threads.internal.model

import im.threads.business.models.FileDescription

data class FileAndMediaItem(val fileDescription: FileDescription, val fileName: String) :
    MediaAndFileItem {
    override val timeStamp: Long
        get() = fileDescription.timeStamp
}
