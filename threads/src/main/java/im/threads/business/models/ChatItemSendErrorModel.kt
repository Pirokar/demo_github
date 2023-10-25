package im.threads.business.models

data class ChatItemSendErrorModel(
    val chatItem: ChatItem? = null,
    val userPhraseUuid: String? = null,
    val message: String? = null
)
