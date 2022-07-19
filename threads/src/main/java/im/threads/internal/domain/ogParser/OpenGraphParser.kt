package im.threads.internal.domain.ogParser

interface OpenGraphParser {
    fun getContents(urlToParse: String): OGData?
}
