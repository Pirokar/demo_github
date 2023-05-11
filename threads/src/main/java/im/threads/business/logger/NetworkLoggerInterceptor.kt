package im.threads.business.logger

import im.threads.business.formatters.JsonFormatter
import im.threads.business.serviceLocator.core.inject
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer

class NetworkLoggerInterceptor(private val isImage: Boolean = false) : Interceptor {
    private val jsonFormatter: JsonFormatter by inject()

    override fun intercept(chain: Interceptor.Chain): Response {
        LoggerEdna.info(getRequestLog(chain))

        val response = chain.proceed(chain.request())

        val newResponse = if (!isImage) {
            val responseBody: ResponseBody? = response.body
            val responseBodyString = response.body?.string() ?: ""

            val newResponse = response.newBuilder()
                .body(responseBodyString.toResponseBody(responseBody?.contentType()))
                .build()

            LoggerEdna.info(getResponseLog(response, responseBodyString))

            newResponse
        } else {
            LoggerEdna.info(getResponseLog(response, null))
            null
        }

        return newResponse ?: response
    }

    private fun getRequestLog(chain: Interceptor.Chain) = StringBuilder().apply {
        append("[REST] ☛ Request url: ${chain.request().url}\n")
        if (chain.connection().toString().isNotBlank()) {
            append("☛ Request connection: ${chain.connection()}\n")
        }
        append("☛ Request headers: ${chain.request().headers}")
        val body = bodyToString(chain.request())
        if (!body.isNullOrBlank()) {
            append("☛ Request body: $body\n")
        }
    }.toString()

    private fun getResponseLog(response: Response, responseBodyString: String?) = StringBuilder().apply {
        append("☚ Response received for url: ${response.request.url}, code: ${response.code}\n")
        append("☚ Request headers: ${response.request.headers}")
        if (!responseBodyString.isNullOrBlank()) {
            append("☚ Request body: ${jsonFormatter.jsonToPrettyFormat(responseBodyString)}\n")
        }
    }.toString()

    private fun bodyToString(request: Request): String? {
        return try {
            val copy = request.newBuilder().build()
            val buffer = Buffer()
            copy.body!!.writeTo(buffer)
            buffer.readUtf8()
        } catch (e: Exception) {
            null
        }
    }
}
