package im.threads.business.ogParser

import io.reactivex.subjects.PublishSubject

interface OpenGraphParser {
    val openGraphParsingStream: PublishSubject<OGData>

    fun getContents(urlToParse: String?): OGData?
    fun getContents(urlToParse: String?, messageText: String?): OGData
    fun getCachedContents(urlToParse: String?): OGData?
}
