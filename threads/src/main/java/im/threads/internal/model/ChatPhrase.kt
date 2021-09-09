package im.threads.internal.model

interface ChatPhrase : ChatItem {
    val id: String?
    val phraseText: String?
    val quote: Quote?
    val fileDescription: FileDescription?
    var found: Boolean
}
