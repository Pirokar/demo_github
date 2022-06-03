package im.threads.internal.model

class Attachment {
    var result: String? = null
    var isSelfie = false
    var optional: Optional? = null
    var state = AttachmentStateEnum.ANY
    var errorCode = ErrorStateEnum.ANY
    var errorMessage = ""

    var name: String? = null
        get() {
            return if (!field.isNullOrEmpty()) field
            else optional?.name
        }

    var type: String? = null
        get() {
            return if (!field.isNullOrEmpty())  field
            else optional?.type
        }
}
