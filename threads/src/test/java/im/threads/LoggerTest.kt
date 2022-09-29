package im.threads

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import im.threads.business.logger.core.LoggerConfig
import im.threads.business.logger.core.LoggerEdna
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class LoggerTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun before() {
        configureLogger()
    }

    @Test
    fun testLogInfo() {
        val logText = "Test log info"
        LoggerEdna.info(logText)

        assertLog().hasInfoMessage(logText)
    }

    @Test
    fun testLogVerbose() {
        val logText = "Test log verbose"
        LoggerEdna.verbose(logText)

        assertLog().hasVerboseMessage(logText)
    }

    @Test
    fun testLogDebug() {
        val logText = "Test log debug"
        LoggerEdna.debug(logText)

        assertLog().hasDebugMessage(logText)
    }

    @Test
    fun testLogWarning() {
        val logText = "Test log warning"
        LoggerEdna.warning(logText)

        assertLog().hasWarnMessage(logText)
    }

    @Test
    fun testLogError() {
        val logText = "Test log error"
        LoggerEdna.error(logText)

        assertLog().hasErrorMessage(logText)
    }

    private fun configureLogger() {
        val loggerConfig = LoggerConfig.Builder(context).build()
        LoggerEdna.init(loggerConfig)
    }

    private fun assertLog(): LogAssert {
        return LogAssert(getLogs())
    }

    private fun getLogs() = ShadowLog.getLogs().filter { it.tag.contains(ENDA_LOGGER_TAG) }

    private class LogAssert(private val items: List<ShadowLog.LogItem>) {
        fun hasVerboseMessage(message: String): LogAssert {
            return hasMessage(Log.VERBOSE, message)
        }

        fun hasDebugMessage(message: String): LogAssert {
            return hasMessage(Log.DEBUG, message)
        }

        fun hasInfoMessage(message: String): LogAssert {
            return hasMessage(Log.INFO, message)
        }

        fun hasWarnMessage(message: String): LogAssert {
            return hasMessage(Log.WARN, message)
        }

        fun hasErrorMessage(message: String): LogAssert {
            return hasMessage(Log.ERROR, message)
        }

        private fun hasMessage(priority: Int, message: String): LogAssert {
            val filteredItem = items.firstOrNull { it.type == priority && it.msg.contains(message) }
            assert(filteredItem != null)
            return this
        }
    }

    private companion object {
        private const val ENDA_LOGGER_TAG = "EdnaLogger"
    }
}
