package im.threads.business.transport

import androidx.test.core.app.ApplicationProvider
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import im.threads.ednaMockThreadsGateProviderUid
import im.threads.ednaMockThreadsGateUrl
import im.threads.ednaMockUrl
import im.threads.ui.config.ConfigBuilder
import im.threads.ui.core.ThreadsLib
import org.junit.Assert
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MessageParserTest {
    private val messageParser = MessageParser()

    @Test
    fun whenFullMessageIsNull_thenReturnsNull() {
        val result = messageParser.format(123456789L, "short message", null)
        assertNull(result)
    }

    @Test
    fun whenChatItemTypeIsTyping_thenReturnsNull() {
        try {
            ThreadsLib.getInstance()
        } catch (exc: Exception) {
            val configBuilder = ConfigBuilder(ApplicationProvider.getApplicationContext())
                .historyLoadingCount(50)
                .isDebugLoggingEnabled(true)
                .showAttachmentsButton()
                .serverBaseUrl(ednaMockUrl)
                .datastoreUrl(ednaMockUrl)
                .threadsGateUrl(ednaMockThreadsGateUrl)
                .threadsGateProviderUid(ednaMockThreadsGateProviderUid)
                .setNewChatCenterApi()

            ThreadsLib.init(configBuilder)
        }

        val fullMessageMock = mock(JsonObject::class.java)
        Mockito.`when`(fullMessageMock.get("type")).thenReturn(JsonPrimitive("TYPING"))
        val result = messageParser.format(123456789L, "short message", fullMessageMock)
        assertNull(result)
    }

    @Test
    fun whenTypeExistsInFullMessage_thenReturnsCorrectType() {
        val fullMessageMock = mock(JsonObject::class.java)
        Mockito.`when`(fullMessageMock.get(MessageAttributes.TYPE)).thenReturn(JsonPrimitive("MESSAGE"))
        val result = messageParser.getType(fullMessageMock)
        Assert.assertEquals("MESSAGE", result)
    }
}
