package im.threads.business.rest.models

import im.threads.business.transport.threadsGate.responses.BaseMessage

data class ConfigResponse(
    val schedule: BaseMessage?,
    val settings: Settings?
)

data class Settings(
    var typingMessagesIntervalSeconds: Int = 3
)
