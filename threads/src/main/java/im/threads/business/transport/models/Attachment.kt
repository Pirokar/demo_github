package im.threads.business.transport.models

import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.models.enums.ErrorStateEnum

class Attachment(
    val id: Long = 0,
    val result: String? = null,
    val originalUrl: String? = null,
    val name: String? = null,
    val size: Long = 0,
    val type: String? = null,
    var state: AttachmentStateEnum = AttachmentStateEnum.ANY,
    var errorCode: String? = null,
    val errorMessage: String = ""
) {
    fun getErrorCodeState(): ErrorStateEnum {
        errorCode?.let {
            return ErrorStateEnum.errorStateEnumFromString(it)
        }
        return ErrorStateEnum.ANY
    }
}
