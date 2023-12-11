package im.threads.business.models

import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.models.enums.ErrorStateEnum
import im.threads.business.models.enums.ErrorStateEnum.Companion.errorStateEnumFromString

class Attachment {
    var result: String? = null
    var optional: Optional? = null
    var state = AttachmentStateEnum.ANY
    var link: String? = null
    var errorCode: String? = null
    var errorMessage = ""

    var name: String? = null
        get() {
            return if (!field.isNullOrEmpty()) {
                field
            } else {
                optional?.name
            }
        }

    var type: String? = null
        get() {
            return if (!field.isNullOrEmpty()) {
                field
            } else {
                optional?.type
            }
        }

    fun getErrorCodeState(): ErrorStateEnum {
        errorCode?.let {
            return errorStateEnumFromString(it)
        }
        return ErrorStateEnum.ANY
    }
}
