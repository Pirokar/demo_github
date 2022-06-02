package im.threads.internal.model

class Attachment {
    var result: String? = null
    var isSelfie = false
    var optional: Optional? = null
    var state = AttachmentStateEnum.ANY
    var errorCode = ErrorStateEnum.ANY
    var errorMessage = ""
    var name: String? = null
    var type: String? = null

    fun getName(): String? {
        if (!name.isNullOrEmpty())
            return name
        optional?.let {
            return it.name
        }
        return null
    }

    fun getType(): String? {
        if (!type.isNullOrEmpty())
            return type
        optional?.let {
            return it.type
        }
        return null
    }
}