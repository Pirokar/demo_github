package im.threads.business.transport.threadsGate.responses

import com.google.gson.JsonObject
import im.threads.business.transport.threadsGate.Action

class BaseResponse {
    val action: Action? = null
    val correlationId: String? = null
    val data: JsonObject? = null
}
