package im.threads.internal.model

import androidx.core.util.ObjectsCompat

class Quote(
    var uuid: String,
    var phraseOwnerTitle: String?,
    val text: String?,
    var fileDescription: FileDescription?,
    val timeStamp: Long
) {
    var isFromConsult = false
    var quotedPhraseConsultId: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val quote = other as Quote
        return if (!ObjectsCompat.equals(text, quote.text)) false else ObjectsCompat.equals(
            fileDescription,
            quote.fileDescription
        )
    }

    override fun hashCode(): Int {
        var result = text?.hashCode() ?: 0
        result = 31 * result + if (fileDescription != null) fileDescription.hashCode() else 0
        return result
    }

    override fun toString(): String {
        return "Quote{" +
                "phraseOwnerTitle='" + phraseOwnerTitle + '\'' +
                ", text='" + text + '\'' +
                ", fileDescription=" + fileDescription +
                ", timeStamp=" + timeStamp +
                ", isFromConsult=" + isFromConsult +
                ", quotedPhraseConsultId='" + quotedPhraseConsultId + '\'' +
                '}'
    }

    fun hasSameContent(quote: Quote?): Boolean {
        if (quote == null) {
            return false
        }
        var hasSameContent = (ObjectsCompat.equals(uuid, quote.uuid)
                && ObjectsCompat.equals(phraseOwnerTitle, quote.phraseOwnerTitle)
                && ObjectsCompat.equals(text, quote.text)
                && ObjectsCompat.equals(timeStamp, quote.timeStamp))
        if (fileDescription != null) {
            hasSameContent =
                hasSameContent && fileDescription!!.hasSameContent(quote.fileDescription)
        }
        return hasSameContent
    }
}