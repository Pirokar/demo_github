package im.threads.business.extensions

import im.threads.business.models.MessageFromHistory
import im.threads.business.rest.models.SearchResponse
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExtensionsTest {
    @Test
    fun whenCreateException_thenMessageContainsText() {
        val exception = Exception("Test exception")
        val expectedMessage = "Message: Test exception"
        val actualMessage = exception.fullLogString()

        assert(actualMessage.startsWith(expectedMessage))
    }

    @Test
    fun whenCreateSeveralMessages_thenSearchShowIt() {
        val message1 = MessageFromHistory().apply {
            uuid = "1"
            text = "message1"
        }

        val message2 = MessageFromHistory().apply {
            uuid = "2"
            text = "message2"
        }

        val response1 = SearchResponse().apply {
            total = 1
            pages = 1
            content = arrayListOf(message1)
        }

        val response2 = SearchResponse().apply {
            total = 2
            pages = 2
            content = arrayListOf(message2)
        }

        val resultResponse = response1 plus response2

        assertEquals(2, resultResponse.total)
        assertEquals(2, resultResponse.pages)
        assertEquals(listOf("message1", "message2"), resultResponse.content?.map { it.text })
    }
}
