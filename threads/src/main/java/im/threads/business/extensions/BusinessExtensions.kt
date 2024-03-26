package im.threads.business.extensions

import android.os.Build
import im.threads.business.models.MessageFromHistory
import im.threads.business.rest.models.SearchResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.PrintWriter
import java.io.StringWriter

suspend fun <T> withMainContext(block: CoroutineScope.() -> T) = withContext(Dispatchers.Main, block)

fun Throwable.fullLogString(): String {
    val stringWriter = StringWriter()
    this.printStackTrace(PrintWriter(stringWriter))
    val stackTrace = buildStackTraceString(this.stackTrace)
    val message = this.message

    return "Message: $message, stacktrace:\n$stackTrace"
}

private fun buildStackTraceString(elements: Array<StackTraceElement>?): String {
    val sb = StringBuilder()
    if (!elements.isNullOrEmpty()) {
        for (element in elements) {
            sb.append("$element\n")
        }
    }
    return sb.toString()
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

fun isUnitTest() = Build.DEVICE == "robolectric" && Build.PRODUCT == "robolectric"
