package im.threads.business.transport.threadsGate.requests

import com.google.gson.JsonObject
import im.threads.business.transport.threadsGate.Action

class SendMessageRequest(correlationId: String?, data: Data?) : BaseRequest<SendMessageRequest.Data?>(
    Action.SEND_MESSAGE,
    correlationId,
    data
) {
    class Data(
        private val deviceAddress: String?,
        private val content: JsonObject,
        private val important: Boolean,
        private val messageId: String
    )
}
