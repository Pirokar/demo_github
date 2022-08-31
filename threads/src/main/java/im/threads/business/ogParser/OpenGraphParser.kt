package im.threads.business.ogParser

interface OpenGraphParser {
    fun getContents(urlToParse: String?): OGData?
    fun getContents(urlToParse: String?, messageText: String?): OGData
    fun getCachedContents(urlToParse: String?): OGData?
}
