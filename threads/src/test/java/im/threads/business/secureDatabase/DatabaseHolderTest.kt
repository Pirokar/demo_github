package im.threads.business.secureDatabase

import androidx.test.core.app.ApplicationProvider
import im.threads.business.models.MessageStatus
import im.threads.business.models.UserPhrase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class DatabaseHolderTest {
    private lateinit var database: DatabaseHolder

    @Before
    fun before() {
        database = DatabaseHolder(ApplicationProvider.getApplicationContext())
    }

    @After
    fun after() {
        database.cleanDatabase()
        database.closeInstance()
    }

    @Test
    fun whenPutItems_thenGetReturnIt() {
        Thread.sleep(1000)
        val items = listOf(
            UserPhrase("2332fvd2323", "hello", null, Date().time, null, 2323),
            UserPhrase("2332fvd2324", "hello2", null, Date().time + 100, null, 2324)
        )
        database.putChatItems(items)
        val insertedItems = database.getChatItems(0, 100)
        assert(
            insertedItems.size == 2 &&
                (insertedItems[0] as UserPhrase).id == "2332fvd2323" &&
                (insertedItems[1] as UserPhrase).id == "2332fvd2324" &&
                (insertedItems[0] as UserPhrase).phraseText == "hello" &&
                (insertedItems[1] as UserPhrase).phraseText == "hello2" &&
                (insertedItems[0] as UserPhrase).threadId == 2323L &&
                (insertedItems[1] as UserPhrase).threadId == 2324L
        )
    }

    @Test
    fun whenPutItems_thenGetWithLimit1ReturnsTheFirst() {
        val items = listOf(
            UserPhrase("2332fvd2323", "hello", null, Date().time, null, 2323),
            UserPhrase("2332fvd2324", "hello2", null, Date().time + 100, null, 2324)
        )
        database.putChatItems(items)
        val insertedItems = database.getChatItems(0, 1)
        assert(
            insertedItems.size == 1 &&
                (insertedItems[0] as UserPhrase).id == "2332fvd2324" &&
                (insertedItems[0] as UserPhrase).phraseText == "hello2" &&
                (insertedItems[0] as UserPhrase).threadId == 2324L
        )
    }

    @Test
    fun whenPutItems_thenGetWithOffset1ReturnsTheLast() {
        val items = listOf(
            UserPhrase("2332fvd2323", "hello", null, Date().time, null, 2323),
            UserPhrase("2332fvd2324", "hello2", null, Date().time + 100, null, 2324)
        )
        database.putChatItems(items)
        val insertedItems = database.getChatItems(1, 100)
        assert(
            insertedItems.size == 1 &&
                (insertedItems[0] as UserPhrase).id == "2332fvd2323" &&
                (insertedItems[0] as UserPhrase).phraseText == "hello" &&
                (insertedItems[0] as UserPhrase).threadId == 2323L
        )
    }

    @Test
    fun whenPutSendingItems_thenGetReturnIt() {
        val items = listOf(
            UserPhrase("2332fvd2323", "sent_item", null, Date().time, null, MessageStatus.SENT, 2323),
            UserPhrase("2332fvd2324", "sending_item", null, Date().time + 100, null, MessageStatus.SENDING, 2324),
            UserPhrase("2332fvd2325", "sending_item2", null, Date().time + 200, null, MessageStatus.SENDING, 2325)
        )
        database.putChatItems(items)
        val sendingItems = database.getSendingChatItems()
        assert(
            sendingItems.size == 2 &&
                sendingItems[0].id == "2332fvd2324" &&
                sendingItems[1].id == "2332fvd2325" &&
                sendingItems[0].phraseText == "sending_item" &&
                sendingItems[1].phraseText == "sending_item2" &&
                sendingItems[0].threadId == 2324L &&
                sendingItems[1].threadId == 2325L
        )
    }
}
