package im.threads.internal.transport.models

data class SpeechMessageUpdatedContent(
    val uuid: String? = null,
    val speechStatus: String? = null,
    val attachments: List<Attachment>? = null
)
