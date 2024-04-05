package edna.chatcenter.demo

import androidx.test.espresso.PerformException
import io.github.kakaocup.kakao.common.views.KBaseView
import io.github.kakaocup.kakao.recycler.KRecyclerView
import junit.framework.AssertionFailedError
import java.util.concurrent.TimeoutException

/**
 * Ожидает видимости View с [timeout] параметром.
 */
fun KRecyclerView.waitListForNotEmpty(timeout: Long) {
    val endTime = System.currentTimeMillis() + timeout

    do {
        val isVisibleAndNotEmpty = try {
            this.isVisible()
            assert(this.getSize() > 0)
            true
        } catch (exc: PerformException) {
            false
        } catch (exc: AssertionError) {
            false
        }

        if (isVisibleAndNotEmpty) {
            return
        }
        Thread.sleep(50)
    } while (System.currentTimeMillis() < endTime)

    throw PerformException.Builder()
        .withActionDescription("waitListForNotEmpty")
        .withCause(TimeoutException("Waited $timeout milliseconds"))
        .build()
}

fun assert(messageIfFails: String?, function: () -> Unit) {
    try {
        function.invoke()
    } catch (exc: AssertionFailedError) {
        throw AssertionError(messageIfFails)
    }
}

fun assert(messageIfFails: String?, vararg function: () -> Unit) {
    var message = messageIfFails
    try {
        if (function.asList().isEmpty()) {
            message = "Отсутствует assertion для проверки"
            throw AssertionFailedError()
        }
        function.forEach { it.invoke() }
    } catch (exc: AssertionFailedError) {
        throw AssertionError(message)
    }
}

/**
 * Ожидает видимости view в иерархии.
 * После отпускает поток
 */
fun <T> KBaseView<T>.waitForExists(timeoutMs: Long) {
    val startTime = System.currentTimeMillis()
    val timeStep = 100L

    do {
        var isVisible = true
        try {
            this.isVisible()
        } catch (exc: AssertionFailedError) {
            isVisible = false
        }
        if (isVisible) {
            return
        } else {
            Thread.sleep(timeStep)
        }
    } while (System.currentTimeMillis() - startTime < timeoutMs)
}
