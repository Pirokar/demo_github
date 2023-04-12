package im.threads.business.logger

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer

class NetworkLoggerInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        LoggerEdna.info(getRequestLog(chain))

        val response = chain.proceed(chain.request())

        val responseBody: ResponseBody? = response.body
        val responseBodyString = response.body?.string() ?: ""

        val newResponse = response.newBuilder()
            .body(responseBodyString.toResponseBody(responseBody?.contentType()))
            .build()

        LoggerEdna.info(getResponseLog(response, responseBodyString))

        return newResponse
    }

    private fun getRequestLog(chain: Interceptor.Chain) = StringBuilder().apply {
        append("[REST] ☛ Request url: ${chain.request().url}\n")
        append("☛ Request connection: ${chain.connection()}\n")
        append("☛ Request headers: ${chain.request().headers}")
        append("☛ Request body: ${bodyToString(chain.request())}\n")
    }.toString()

    private fun getResponseLog(response: Response, responseBodyString: String) = StringBuilder().apply {
        append("☚ Response received for url: ${response.request.url}\n")
        append("☚ Request headers: ${response.request.headers}")
        append("☚ Request body: ${jsonToPrettyFormat(responseBodyString)}\n")
    }.toString()

    private fun bodyToString(request: Request): String? {
        return try {
            val copy = request.newBuilder().build()
            val buffer = Buffer()
            copy.body!!.writeTo(buffer)
            buffer.readUtf8()
        } catch (e: Exception) {
            "no body"
        }
    }

    private fun jsonToPrettyFormat(jsonString: String?): String {
        return try {
            val jsonObject = JsonParser.parseString(jsonString)
            val gson: Gson = GsonBuilder().setPrettyPrinting().create()
            gson.toJson(jsonObject)
        } catch (exc: Exception) {
            "Cannot create PrettyJson. Input json: $jsonString"
        }
    }
}
