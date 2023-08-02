package im.threads.business.models

import androidx.core.util.ObjectsCompat
import com.google.gson.annotations.SerializedName

class ConsultConnectionMessage : ConsultChatPhrase, ChatItem, SystemMessage {
    val connectionType: String?
    val name: String?
    val sex: Boolean
    val status: String?
    val uuid: String?
    val title: String?
    val orgUnit: String?
    val role: String?
    private val text: String?
    override val threadId: Long
    override val timeStamp: Long

    @SerializedName("display")
    val display: Boolean

    /**
     * Используется в старой БД.
     */
    @Deprecated("")
    constructor(
        uuid: String?,
        consultId: String?,
        type: String?,
        name: String?,
        sex: Boolean,
        date: Long,
        avatarPath: String?,
        status: String?,
        title: String?,
        orgUnit: String?,
        displayMessage: Boolean,
        text: String?,
        threadId: Long
    ) : super(avatarPath, consultId) {
        this.uuid = uuid
        connectionType = type
        this.name = name
        this.sex = sex
        timeStamp = date
        this.status = status
        this.title = title
        this.orgUnit = orgUnit
        role = null
        display = displayMessage
        this.text = text
        this.threadId = threadId
    }

    constructor(
        uuid: String?,
        consultId: String?,
        type: String?,
        name: String?,
        sex: Boolean,
        date: Long,
        avatarPath: String?,
        status: String?,
        title: String?,
        orgUnit: String?,
        role: String?,
        displayMessage: Boolean,
        text: String?,
        threadId: Long
    ) : super(avatarPath, consultId) {
        this.uuid = uuid
        connectionType = type
        this.name = name
        this.sex = sex
        timeStamp = date
        this.status = status
        this.title = title
        this.orgUnit = orgUnit
        this.role = role
        display = displayMessage
        this.text = text
        this.threadId = threadId
    }

    override fun getType(): String {
        return connectionType ?: ""
    }

    override fun getText(): String {
        return text ?: ""
    }

    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        return if (otherItem is ConsultConnectionMessage) {
            ObjectsCompat.equals(uuid, otherItem.uuid)
        } else {
            false
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ConsultConnectionMessage
        return sex == that.sex && timeStamp == that.timeStamp && display == that.display &&
            ObjectsCompat.equals(uuid, that.uuid) &&
            ObjectsCompat.equals(connectionType, that.connectionType) &&
            ObjectsCompat.equals(name, that.name) &&
            ObjectsCompat.equals(status, that.status) &&
            ObjectsCompat.equals(title, that.title) &&
            ObjectsCompat.equals(orgUnit, that.orgUnit) &&
            ObjectsCompat.equals(role, that.role) &&
            ObjectsCompat.equals(text, that.text)
    }

    override fun hashCode(): Int {
        return ObjectsCompat.hash(
            uuid, connectionType, name, sex, timeStamp, status,
            title, orgUnit, role, display, text
        )
    }
}
