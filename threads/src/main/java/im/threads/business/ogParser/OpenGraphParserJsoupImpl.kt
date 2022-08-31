package im.threads.business.ogParser

import android.net.Uri
import im.threads.business.logger.LoggerEdna
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.security.KeyManagementException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

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
    private fun socketFactory(): SSLSocketFactory {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        })

        try {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            return sslContext.socketFactory
        } catch (e: Exception) {
            when (e) {
                is RuntimeException, is KeyManagementException -> {
                    throw RuntimeException("Failed to create a SSL socket factory", e)
                }
                else -> throw e
            }
        }
    }

    /**
     * Метод для запроса og data. Возвращает распарсенные данные. Работает синхронно,
     *  необходимо обернуть в фоновый поток
     * @param urlToParse ссылка на сайт, где необходимо запросить Open Graph
     * @param messageText текст всего сообщения
     */
    override fun getContents(urlToParse: String?, messageText: String?): OGData {
        val content = getContents(urlToParse)
        return content?.also {
            it.messageText = messageText
        } ?: OGData().also { it.messageText = messageText }
    }

    /**
     * Метод для запроса og data онлайн. Возвращает распарсенные данные. Работает синхронно,
     *  необходимо обернуть в фоновый поток
     * @param urlToParse ссылка на сайт, где необходимо запросить Open Graph
     */
    override fun getContents(urlToParse: String?): OGData? {
        if (urlToParse == null) return null

        return try {
            val response = Jsoup.connect(urlToParse)
                .sslSocketFactory(socketFactory())
                .ignoreContentType(true)
                .userAgent("Mozilla")
                .referrer("http://www.google.com")
                .timeout(12000)
                .followRedirects(true)
                .execute()

            val doc = response.parse()
            val result = organizeFetchedData(doc).apply {
                parsedUrl = urlToParse
            }
            existedOpenGraphs[getHostAndPath(urlToParse)] = result

            result
        } catch (e: Exception) {
            LoggerEdna.error("Error when parsing OG data!", e)
            null
        }
    }

    /**
     * Метод для запроса og data онлайн. Возвращает распарсенные данные из кэша, если доступны.
     * @param urlToParse ссылка на сайт, где необходимо запросить Open Graph
     */
    override fun getCachedContents(urlToParse: String?): OGData? {
        if (urlToParse == null) return null

        val hostAndPath = getHostAndPath(urlToParse)
        val cachedData = existedOpenGraphs[hostAndPath]

        LoggerEdna.info("Getting cached OpenGraph data. Url: $urlToParse, data: $cachedData")

        return cachedData
    }

    private fun getHostAndPath(urlToParse: String?): String {
        return Uri.parse(urlToParse)?.let {
            var host = it.host ?: ""
            if (host.contains("www.")) {
                host = host.replace("www.", "")
            }
            "$host${it.path ?: ""}${it.query?.let { query -> "?$query" } ?: ""}"
        } ?: urlToParse ?: ""
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
