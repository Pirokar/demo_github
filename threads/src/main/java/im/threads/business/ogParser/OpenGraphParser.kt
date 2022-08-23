package im.threads.business.ogParser

interface OpenGraphParser {
    fun getContents(urlToParse: String): OGData?
}
