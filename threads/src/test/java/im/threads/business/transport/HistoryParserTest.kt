package im.threads.business.transport

import androidx.test.core.app.ApplicationProvider
import im.threads.business.core.ContextHolder
import im.threads.business.models.MessageFromHistory
import im.threads.business.models.UserPhrase
import im.threads.business.rest.models.HistoryResponse
import im.threads.business.serviceLocator.core.startEdnaLocator
import im.threads.business.serviceLocator.coreSLModule
import im.threads.ui.serviceLocator.uiSLModule
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HistoryParserTest {

    @Before
    fun before() {
        ContextHolder.context = ApplicationProvider.getApplicationContext()
        startEdnaLocator { modules(coreSLModule, uiSLModule) }
    }

    @Test
    fun whenGetChatItemsIsCalledWithNullResponse_thenReturnsEmptyList() {
        val result = HistoryParser.getChatItems(null)
        assert(result.isEmpty())
    }

    @Test
    fun whenGetChatItemsIsCalledWithNonNullResponse_thenReturnsCorrectList() {
        val messages = listOf(MessageFromHistory())
        val response = HistoryResponse(messages)
        val result = HistoryParser.getChatItems(response)
        assert(messages.size == result.size)
        assert(result[0] is UserPhrase)
    }
}
