package im.threads.internal.domain.ogParser

data class OGData(
    var title: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var url: String = "",
    var siteName: String = "",
    var type: String = ""
) {
    fun isEmpty() = areTextsEmpty() && imageUrl.isEmpty()

    fun areTextsEmpty() = title.isEmpty() && description.isEmpty() && url.isEmpty()

    override fun toString(): String {
        return "Open Graph Data:[title:$title, desc: $description, imageUrl: imageUrl, url:$url," +
            " siteName: $siteName, type: $type]"
    }
}
