package im.threads.business.transport.threadsGate.responses

import com.google.gson.JsonObject
import java.util.Date

class BaseMessage {
    val messageId: String? = null
    val sentAt: Date? = null
    val notification: String? = null
    val content: JsonObject? = null
}
