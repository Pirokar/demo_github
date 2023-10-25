package im.threads.business.models

interface ChatPhrase : ChatItem {
    val id: String?
    val phraseText: String?
    val quote: Quote?
    val fileDescription: FileDescription?
    var found: Boolean
}
