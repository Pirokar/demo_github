package im.threads.business.extensions

import im.threads.business.models.MessageFromHistory
import im.threads.business.rest.models.SearchResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.PrintWriter
import java.io.StringWriter

suspend fun <T> withMainContext(block: CoroutineScope.() -> T) = withContext(Dispatchers.Main, block)

fun Exception.fullLogString(): String {
    val stringWriter = StringWriter()
    this.printStackTrace(PrintWriter(stringWriter))
    val stackTrace = stringWriter.toString()
    val message = this.message

    return "Message: $message, stacktrace:\n$stackTrace"
}

infix fun SearchResponse?.plus(newResponse: SearchResponse?): SearchResponse {
    val resultResponse = newResponse?.apply {
        if (total == null) total = this@plus?.total
        if (pages == null) pages = this@plus?.pages

        val result = arrayListOf<MessageFromHistory>()
        result.addAll(this@plus?.content ?: listOf())
        result.addAll(content ?: listOf())

        content = result
    } ?: (this ?: SearchResponse())

    return resultResponse
}
