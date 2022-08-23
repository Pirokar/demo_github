package im.threads.internal.model

import im.threads.business.models.ChatItem

data class ChatItemSendErrorModel(
    val chatItem: ChatItem? = null,
    val userPhraseUuid: String? = null,
    val message: String? = null
)
