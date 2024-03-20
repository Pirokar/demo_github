package im.threads.business.extensions

import android.os.Build
import im.threads.business.models.MessageFromHistory
import im.threads.business.rest.models.SearchResponse
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.coroutines.CoroutineContext

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

/**
 * Отбрасывает значения для заданного интервала [delay]. По умолчанию 1000 ms.
 * Можно задать фильтр значений
 * @param delay интервал, в течение которого будут игнорироваться все последующие значения
 * @param context CoroutineContext, по умолчанию Dispatchers.Unconfined
 * @param filter nullable фильтр, которые может отсеять значения, чтобы они
 * изначально не участвовали в процессе троттлинга и не меняли таймер
 */
fun <E> Flow<E>.throttle(
    delay: Long = 1000,
    context: CoroutineContext = Dispatchers.Unconfined,
    filter: ((E) -> Boolean)? = null
): Flow<E> = flow {
    var nextTime = System.currentTimeMillis()
    collect { value ->
        val curTime = System.currentTimeMillis()
        if ((filter == null || filter(value)) && curTime >= nextTime) {
            nextTime = curTime + delay
            emit(value)
        }
    }
}.flowOn(context)

/**
 * Преобразование [BehaviorSubject] в Kotlin Flow
 */
fun <T> BehaviorSubject<T>.toFlow() = callbackFlow {
    val disposable = subscribe { trySendBlocking(it).getOrThrow() }
    awaitClose { disposable.dispose() }
}
