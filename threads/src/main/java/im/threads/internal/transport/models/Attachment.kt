package im.threads.internal.transport.models

import im.threads.internal.model.AttachmentStateEnum
import im.threads.internal.model.ErrorStateEnum

class Attachment(
    val id: Long = 0,
    val result: String? = null,
    val originalUrl: String? = null,
    val name: String? = null,
    val size: Long = 0,
    val type: String? = null,
    var state: AttachmentStateEnum = AttachmentStateEnum.ERROR,
    val errorCode: ErrorStateEnum = ErrorStateEnum.ANY,
    val errorMessage: String = ""
)
