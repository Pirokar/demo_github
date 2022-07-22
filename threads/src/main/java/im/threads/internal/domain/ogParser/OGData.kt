package im.threads.internal.domain.ogParser

/**
 * Содержит набор параметров, возвращаемых после парсинга meta data у html.
 */
data class OGData(
    var title: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var url: String = "",
    var siteName: String = "",
    var type: String = ""
) {
    fun isEmpty() = title.isEmpty() && description.isEmpty() && url.isEmpty()

    override fun toString(): String {
        return "Open Graph Data:[title:$title, desc: $description, imageUrl: imageUrl, url:$url," +
            " siteName: $siteName, type: $type]"
    }
}
