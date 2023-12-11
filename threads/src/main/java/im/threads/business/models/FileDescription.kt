package im.threads.business.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.core.util.ObjectsCompat
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.models.enums.AttachmentStateEnum.Companion.fromString
import im.threads.business.models.enums.ErrorStateEnum
import im.threads.business.models.enums.ErrorStateEnum.Companion.errorStateEnumFromString
import im.threads.business.utils.FileUtils
import io.reactivex.subjects.PublishSubject

class FileDescription(var from: String?, var fileUri: Uri?, var size: Long, var timeStamp: Long) :
    Parcelable {
    val onCompleteSubject = PublishSubject.create<FileDescriptionUri>()
    var downloadPath: String? = null
    var originalPath: String? = null
    var incomingName: String? = null
    var mimeType: String? = null
    var downloadProgress = 0
    var isDownloadError = false
    var state = AttachmentStateEnum.ANY
    var errorCode = ErrorStateEnum.ANY
    var errorMessage: String? = ""
    var voiceFormattedDuration: String = ""
    var offerLink: String? = null
    private var smallFileDescription: FileDescription? = null

    fun getPreviewFileDescription(): FileDescription? {
        if (isFromAssets()) {
            return this
        }

        if (smallFileDescription == null && FileUtils.isImage(this)) {
            smallFileDescription = FileDescription(from, null, size, timeStamp)
            smallFileDescription?.downloadPath = "$downloadPath?size=small"
            smallFileDescription?.originalPath = "$originalPath?size=small"
            smallFileDescription?.incomingName = FileUtils.generatePreviewFileName(incomingName)
            smallFileDescription?.state = state
        }
        return smallFileDescription
    }

    internal fun isFromAssets(): Boolean {
        downloadPath?.let {
            if (it.startsWith("file:") && it.contains("android_asset")) {
                return true
            }
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileDescription) return false
        if (size != other.size) return false
        if (timeStamp != other.timeStamp) return false
        return if (!ObjectsCompat.equals(mimeType, other.mimeType)) {
            false
        } else {
            ObjectsCompat.equals(
                from,
                other.from
            )
        }
    }

    override fun hashCode(): Int {
        return ObjectsCompat.hash(size, timeStamp, mimeType, from)
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(state.state)
        dest.writeString(errorCode.state)
        dest.writeString(errorMessage)
        dest.writeString(from)
        dest.writeParcelable(fileUri, 0)
        dest.writeString(downloadPath)
        dest.writeString(incomingName)
        dest.writeString(originalPath)
        dest.writeString(mimeType)
        dest.writeLong(size)
        dest.writeLong(timeStamp)
        dest.writeInt(downloadProgress)
    }

    override fun toString(): String {
        return "FileDescription{" +
            "from='" + from + '\'' +
            ", fileUri='" + fileUri + '\'' +
            ", downloadPath='" + downloadPath + '\'' +
            ", incomingName='" + incomingName + '\'' +
            ", size=" + size +
            ", timeStamp=" + timeStamp +
            ", downloadProgress=" + downloadProgress +
            ", state=" + state.state +
            ", errorCode=" + errorCode.state +
            ", errorMessage=" + errorMessage +
            '}'
    }

    fun hasSameContent(fileDescription: FileDescription?): Boolean {
        return if (fileDescription == null) {
            false
        } else {
            (
                ObjectsCompat.equals(from, fileDescription.from) &&
                    ObjectsCompat.equals(fileUri, fileDescription.fileUri) &&
                    ObjectsCompat.equals(timeStamp, fileDescription.timeStamp) &&
                    ObjectsCompat.equals(downloadPath, fileDescription.downloadPath) &&
                    ObjectsCompat.equals(size, fileDescription.size) &&
                    ObjectsCompat.equals(incomingName, fileDescription.incomingName) &&
                    ObjectsCompat.equals(mimeType, fileDescription.mimeType) &&
                    ObjectsCompat.equals(downloadProgress, fileDescription.downloadProgress)
                )
        }
    }

    companion object {
        @JvmField
        val CREATOR: Creator<FileDescription> = object : Creator<FileDescription> {
            override fun createFromParcel(source: Parcel): FileDescription {
                val state = source.readString()
                val errorCode = source.readString()
                val errorMessage = source.readString()
                val from = source.readString()
                val filePath = source.readParcelable<Uri>(Uri::class.java.classLoader)
                val downloadPath = source.readString()
                val incomingName = source.readString()
                val originalPath = source.readString()
                val mimeType = source.readString()
                val size = source.readLong()
                val timeStamp = source.readLong()
                val progress = source.readInt()
                val fd = FileDescription(from, filePath, size, timeStamp)
                fd.state = fromString(state!!)
                fd.errorCode = errorStateEnumFromString(errorCode!!)
                fd.errorMessage = errorMessage
                fd.incomingName = incomingName
                fd.mimeType = mimeType
                fd.downloadPath = downloadPath
                fd.originalPath = originalPath
                fd.downloadProgress = progress
                return fd
            }

            override fun newArray(size: Int): Array<FileDescription?> {
                return arrayOfNulls(size)
            }
        }
    }
}
