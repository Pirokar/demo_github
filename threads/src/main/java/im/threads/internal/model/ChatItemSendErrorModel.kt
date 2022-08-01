package im.threads.internal.model

data class ChatItemSendErrorModel(
    val chatItem: ChatItem? = null,
    val userPhraseUuid: String? = null,
    val message: String? = null
)
