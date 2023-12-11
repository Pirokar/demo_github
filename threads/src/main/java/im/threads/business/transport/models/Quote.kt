package im.threads.business.transport.models

import im.threads.business.models.enums.ModificationStateEnum
import java.util.Date

class Quote {
    val id: Long = 0
    val receivedDate: Date? = null
    val uuid: String? = null
    val text: String? = null
    val attachments: List<Attachment>? = null
    val operator: Operator? = null
    val modified = ModificationStateEnum.NONE
}
