package im.threads.business.formatters

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.text.TextUtils
import androidx.core.util.ObjectsCompat
import im.threads.R
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultConnectionMessage
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.SimpleSystemMessage
import im.threads.business.models.Survey
import im.threads.business.utils.FileUtils.isImage

object MessageFormatter {
    fun parseMessageContent(ctx: Context, chatItems: List<ChatItem?>): MessageContent {
        var imagesCount = 0
        var plainFilesCount = 0
        var avatarPath: String? = null
        var imagePath: String? = null
        var phrase = ""
        var sex = false
        var docName = ""
        var consultName: String? = null
        var isNeedAnswer = false
        val unreadMessages: MutableList<ChatItem> = ArrayList()
        for (ci in chatItems) {
            if (ci is ConsultConnectionMessage || ci is ConsultPhrase || ci is SimpleSystemMessage || ci is Survey) {
                unreadMessages.add(ci)
            }
        }
        for (ci in unreadMessages) {
            if (ci is ConsultConnectionMessage) {
                val ccm = ci
                consultName = ccm.name
                phrase = ccm.text
                sex = ccm.sex
                avatarPath = ci.avatarPath
            }
            if (ci is SimpleSystemMessage) {
                phrase = ci.text
            }
            if (ci is ConsultPhrase) {
                isNeedAnswer = true
                if (!TextUtils.isEmpty(ci.phraseText)) {
                    phrase = ci.phraseText ?: ""
                }
                sex = ci.sex
                if (ci.consultName != null) {
                    consultName = ci.consultName
                }
                if (ci.fileDescription != null) {
                    val fileDescription = ci.fileDescription
                    if (isImage(fileDescription)) {
                        imagesCount++
                        imagePath = fileDescription?.downloadPath
                    } else {
                        plainFilesCount++
                        docName = fileDescription?.incomingName ?: ""
                    }
                }
                if (ci.quote?.fileDescription != null) {
                    val fileDescription = ci.quote.fileDescription
                    if (isImage(fileDescription)) {
                        imagesCount++
                        imagePath = fileDescription?.downloadPath
                    } else {
                        plainFilesCount++
                        docName = fileDescription?.incomingName ?: ""
                    }
                }
                avatarPath = ci.avatarPath
            }
            if (ci is Survey) {
                val questions = ci.questions
                if (questions != null && questions.size > 0) {
                    phrase = questions[0].text ?: ""
                }
            }
        }
        var titleText = consultName
        if (plainFilesCount != 0) {
            val send =
                if (sex) ctx.getString(R.string.ecc_send_male) else ctx.getString(R.string.ecc_send_female)
            titleText = "$consultName $send "
            val total = plainFilesCount + imagesCount
            titleText += ctx.resources.getQuantityString(R.plurals.ecc_files, total, total)
            if (TextUtils.isEmpty(phrase)) {
                phrase = if (total == 1) {
                    docName
                } else {
                    ctx.getString(R.string.ecc_touch_to_download)
                }
            }
        } else if (imagesCount != 0) {
            val send =
                if (sex) ctx.getString(R.string.ecc_send_male) else ctx.getString(R.string.ecc_send_female)
            titleText = "$consultName $send " + ctx.resources.getQuantityString(
                R.plurals.ecc_images,
                imagesCount,
                imagesCount
            )
            if (TextUtils.isEmpty(phrase)) {
                phrase = ctx.getString(R.string.ecc_touch_to_look)
            }
        } else if (unreadMessages.size > 1) {
            titleText = ctx.resources.getQuantityString(
                R.plurals.ecc_new_messages,
                unreadMessages.size,
                unreadMessages.size
            )
        }
        return MessageContent(
            titleText,
            phrase ?: "",
            !TextUtils.isEmpty(avatarPath),
            imagesCount != 0,
            plainFilesCount != 0,
            imagesCount,
            unreadMessages.size,
            avatarPath,
            imagePath,
            consultName,
            isNeedAnswer
        )
    }

    class MessageContent : Parcelable {
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

        internal constructor(
            titleText: String?,
            contentText: String,
            hasAvatar: Boolean,
            hasImage: Boolean,
            hasPlainFiles: Boolean,
            imagesCount: Int,
            phrasesCount: Int,
            avatarPath: String?,
            lastImagePath: String?,
            consultName: String?,
            isNeedAnswer: Boolean
        ) {
            this.titleText = titleText
            this.contentText = contentText
            this.hasAvatar = hasAvatar
            this.hasImage = hasImage
            this.imagesCount = imagesCount
            this.phrasesCount = phrasesCount
            this.hasPlainFiles = hasPlainFiles
            this.avatarPath = avatarPath
            this.lastImagePath = lastImagePath
            this.consultName = consultName
            this.isNeedAnswer = isNeedAnswer
        }

        protected constructor(`in`: Parcel) {
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
