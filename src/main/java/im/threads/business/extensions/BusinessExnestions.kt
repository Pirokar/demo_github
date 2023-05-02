package im.threads.business.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.PrintWriter
import java.io.StringWriter

suspend fun <T> CoroutineScope.withMainContext(block: CoroutineScope.() -> T) = withContext(Dispatchers.Main, block)

fun Exception.fullLogString(): String {
    val stringWriter = StringWriter()
    this.printStackTrace(PrintWriter(stringWriter))
    val stackTrace = stringWriter.toString()
    val message = this.message

    return "Message: $message, stacktrace:\n$stackTrace"
}
