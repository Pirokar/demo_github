package im.threads.internal.domain.ogParser

import android.net.Uri
import im.threads.internal.utils.ThreadsLogger
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

private const val OG_TITLE: String = "og:title"
private const val OG_DESCRIPTION: String = "og:description"
private const val OG_TYPE: String = "og:type"
private const val OG_IMAGE: String = "og:image"
private const val OG_URL: String = "og:url"
private const val OG_SITE_NAME: String = "og:site_name"

/**
 * Парсер для Open Graph
 * Этот класс использует библиотеку jsoup, которая загружает url и возвращает парсинг всех данных.
 */
class OpenGraphParserJsoupImpl : OpenGraphParser {
    private val tag = OpenGraphParserJsoupImpl::class.java.simpleName

    /**
     * Метод для запроса og data. Возвращает распарсенные данные. Работает синхронно,
     *  необходимо обернуть в фоновый поток
     * @param urlToParse ссылка на сайт, где необходимо запросить Open Graph
     */
    override fun getContents(urlToParse: String): OGData? {
        val hostAndPath = Uri.parse(urlToParse)?.let {
            var host = it.host ?: ""
            if (host.contains("www.")) {
                host = host.replace("www.", "")
            }
            "$host${it.path ?: ""}${it.query?.let { query -> "?$query" } ?: ""}"
        } ?: urlToParse

        existedOpenGraphs[hostAndPath]?.let {
            return it
        }

        return try {
            val response = Jsoup.connect(urlToParse)
                .ignoreContentType(true)
                .userAgent("Mozilla")
                .referrer("http://www.google.com")
                .timeout(12000)
                .followRedirects(true)
                .execute()
            val doc = response.parse()
            val result = organizeFetchedData(doc)

            existedOpenGraphs[hostAndPath] = result
            result
        } catch (e: Exception) {
            ThreadsLogger.e(tag, "Error when parsing OG data!", e)
            null
        }
    }

    private fun organizeFetchedData(doc: Document): OGData {
        val openGraphContent = OGData()
        val ogTags = doc.select("meta[property^=og:]")
        when {
            ogTags.size > 0 ->
                ogTags.forEachIndexed { index, _ ->
                    val tag = ogTags[index]
                    val property = tag.attr("property")
                    val content = tag.attr("content")
                    when (property) {
                        OG_IMAGE -> {
                            openGraphContent.imageUrl = content
                        }
                        OG_DESCRIPTION -> {
                            openGraphContent.description = content
                        }
                        OG_URL -> {
                            openGraphContent.url = content
                        }
                        OG_TITLE -> {
                            openGraphContent.title = content
                        }
                        OG_SITE_NAME -> {
                            openGraphContent.siteName = content
                        }
                        OG_TYPE -> {
                            openGraphContent.type = content
                        }
                    }
                }
        }
        return openGraphContent
    }

    companion object {
        private val existedOpenGraphs = HashMap<String, OGData>()
            @Synchronized get
    }
}
