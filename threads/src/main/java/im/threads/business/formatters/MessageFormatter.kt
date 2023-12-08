package im.threads.business.formatters

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.core.util.ObjectsCompat

object MessageFormatter {
    class MessageContent protected constructor(`in`: Parcel) : Parcelable {
        val titleText: String?
        val contentText: String
        val hasAvatar: Boolean
        val hasImage: Boolean
        val hasPlainFiles: Boolean
        val imagesCount: Int
        val phrasesCount: Int
        val avatarPath: String?
        val lastImagePath: String?
        val consultName: String?
        val isNeedAnswer: Boolean

        init {
            titleText = `in`.readString()
            contentText = `in`.readString()!!
            hasAvatar = `in`.readByte().toInt() != 0
            hasImage = `in`.readByte().toInt() != 0
            hasPlainFiles = `in`.readByte().toInt() != 0
            imagesCount = `in`.readInt()
            phrasesCount = `in`.readInt()
            avatarPath = `in`.readString()
            lastImagePath = `in`.readString()
            consultName = `in`.readString()
            isNeedAnswer = `in`.readByte().toInt() != 0
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other == null || javaClass != other.javaClass) {
                return false
            }
            val that = other as MessageContent
            return hasAvatar == that.hasAvatar && hasImage == that.hasImage && imagesCount == that.imagesCount && phrasesCount == that.phrasesCount && hasPlainFiles == that.hasPlainFiles && isNeedAnswer == that.isNeedAnswer &&
                ObjectsCompat.equals(titleText, that.titleText) &&
                ObjectsCompat.equals(contentText, that.contentText) &&
                ObjectsCompat.equals(avatarPath, that.avatarPath) &&
                ObjectsCompat.equals(lastImagePath, that.lastImagePath) &&
                ObjectsCompat.equals(consultName, that.consultName)
        }

        override fun hashCode(): Int {
            return ObjectsCompat.hash(
                titleText,
                contentText,
                hasAvatar,
                hasImage,
                imagesCount,
                phrasesCount,
                hasPlainFiles,
                avatarPath,
                lastImagePath,
                consultName,
                isNeedAnswer
            )
        }

        override fun toString(): String {
            return "MessageContent{" +
                "titleText='" + titleText + '\'' +
                ", contentText='" + contentText + '\'' +
                ", hasAvatar=" + hasAvatar +
                ", hasImage=" + hasImage +
                ", hasPlainFiles=" + hasPlainFiles +
                ", imagesCount=" + imagesCount +
                ", phrasesCount=" + phrasesCount +
                ", avatarPath='" + avatarPath + '\'' +
                ", lastImagePath='" + lastImagePath + '\'' +
                ", consultName='" + consultName + '\'' +
                ", isNeedAnswer=" + isNeedAnswer +
                '}'
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(titleText)
            dest.writeString(contentText)
            dest.writeByte((if (hasAvatar) 1 else 0).toByte())
            dest.writeByte((if (hasImage) 1 else 0).toByte())
            dest.writeByte((if (hasPlainFiles) 1 else 0).toByte())
            dest.writeInt(imagesCount)
            dest.writeInt(phrasesCount)
            dest.writeString(avatarPath)
            dest.writeString(lastImagePath)
            dest.writeString(consultName)
            dest.writeByte((if (isNeedAnswer) 1 else 0).toByte())
        }

        companion object {
            @JvmField
            val CREATOR: Creator<MessageContent> = object : Creator<MessageContent> {
                override fun createFromParcel(`in`: Parcel): MessageContent {
                    return MessageContent(`in`)
                }

                override fun newArray(size: Int): Array<MessageContent?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}
