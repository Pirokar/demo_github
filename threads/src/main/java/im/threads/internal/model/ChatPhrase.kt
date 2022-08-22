package im.threads.internal.model

import im.threads.business.models.ChatItem

interface ChatPhrase : ChatItem {
    val id: String?
    val phraseText: String?
    val quote: Quote?
    val fileDescription: FileDescription?
    var found: Boolean
}
