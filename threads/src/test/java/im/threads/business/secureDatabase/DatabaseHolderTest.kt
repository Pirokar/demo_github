package im.threads.business.secureDatabase

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import im.threads.business.formatters.SpeechStatus
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.ConsultRole
import im.threads.business.models.FileDescription
import im.threads.business.models.MessageStatus
import im.threads.business.models.UserPhrase
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.models.enums.ModificationStateEnum
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class DatabaseHolderTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private var database = DatabaseHolder(context)

    @Before
    fun before() {
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
        assert(containsItems(items.toTypedArray(), insertedItems.toTypedArray()))
    }

    @Test
    fun whenPutItems_thenGetWithLimit1ReturnsTheLast() {
        val correctItem = UserPhrase(
            "2332fvd2324",
            "hello2",
            null,
            Date().time + 100,
            null,
            2324
        )
        val items = listOf(
            UserPhrase(
                "2332fvd2323",
                "hello",
                null,
                Date().time,
                null,
                2323
            ),
            correctItem
        )
        database.putChatItems(items)
        val insertedItems = database.getChatItems(0, 1)
        assert(containsItems(arrayOf(correctItem), insertedItems.toTypedArray()))
    }

    @Test
    fun whenPutItems_thenGetWithOffset1ReturnsThe() {
        val correctItem = UserPhrase("2332fvd2323", "hello", null, Date().time, null, 2323)
        val items = listOf(
            UserPhrase(
                "2332fvd2324",
                "hello2",
                null,
                Date().time + 100,
                null,
                2324
            ),
            correctItem
        )
        database.putChatItems(items)
        val insertedItems = database.getChatItems(1, 100)
        assert(containsItems(arrayOf(correctItem), insertedItems.toTypedArray()))
    }

    @Test
    fun whenPutSendingItems_thenGetReturnIt() {
        val correctItems = arrayOf(
            UserPhrase(
                "2332fvd2324",
                "sending_item",
                null,
                Date().time + 100,
                null,
                MessageStatus.SENDING,
                2324
            ),
            UserPhrase(
                "2332fvd2325",
                "sending_item2",
                null,
                Date().time + 200,
                null,
                MessageStatus.SENDING,
                2325
            )
        )
        database.putChatItems(
            listOf(
                *correctItems,
                UserPhrase(
                    "2332fvd2323",
                    "sent_item",
                    null,
                    Date().time,
                    null,
                    MessageStatus.SENT,
                    2323
                )
            )
        )
        val sendingItems = database.getSendingChatItems()
        assert(containsItems(correctItems, sendingItems.toTypedArray()))
    }

    @Test
    fun whenPutNotDeliveredItems_thenGetReturnIt() {
        val correctItems = arrayOf(
            UserPhrase(
                "2332fvd2323",
                "enqueued_item",
                null,
                Date().time,
                null,
                MessageStatus.ENQUEUED,
                2323
            ),
            UserPhrase(
                "2332fvd2324",
                "sending_item",
                null,
                Date().time + 100,
                null,
                MessageStatus.SENDING,
                2324
            ),
            UserPhrase(
                "2332fvd2325",
                "sent_item",
                null,
                Date().time + 200,
                null,
                MessageStatus.SENT,
                2325
            )
        )
        database.putChatItems(
            listOf(
                *correctItems,
                UserPhrase(
                    "2332fvd2326",
                    "delivered_item",
                    null,
                    Date().time + 300,
                    null,
                    MessageStatus.DELIVERED,
                    2326
                )
            )
        )
        val notDeliveredItems = database.getNotDeliveredChatItems()
        assert(containsItems(correctItems, notDeliveredItems.toTypedArray()))
    }

    @Test
    fun whenPutItemWithCorrelationId_thenGetReturnIt() {
        val message = UserPhrase(
            "2332zone2326",
            "Some text",
            null,
            Date().time,
            null,
            MessageStatus.SENT,
            2330
        )
        database.putChatItem(message)
        val insertedItem = database.getChatItemByCorrelationId(message.id)
        assert(message == insertedItem)
    }

    @Test
    fun whenPutItemWithBackendId_thenGetReturnIt() {
        val message = UserPhrase(
            "2332zone2326",
            "Some text",
            null,
            Date().time,
            null,
            MessageStatus.SENT,
            2330
        ).apply {
            backendMessageId = "backend2334566"
        }

        database.putChatItem(message)
        val insertedItem = database.getChatItemByBackendMessageId(message.backendMessageId)
        assert(message == insertedItem)
    }

    @Test
    fun whenPutFileDescriptions_thenGetReturnIt() {
        val items = listOf(
            UserPhrase(
                "2332fileDescription1",
                "hello",
                null,
                Date().time,
                FileDescription(
                    "From Russia with love",
                    Uri.parse(context.filesDir.path),
                    3434344L,
                    Date().time
                ),
                2323
            ),
            UserPhrase(
                "2332fileDescription2",
                "hi",
                null,
                Date().time,
                FileDescription(
                    "From Rostov with love",
                    Uri.parse(context.filesDir.path),
                    555555L,
                    Date().time
                ),
                2324
            )
        )
        database.putChatItems(items)
        val fileDescriptions = database.getAllFileDescriptions().blockingGet()
        assert(
            fileDescriptions?.firstOrNull { it?.equals(items[0].fileDescription) == true } != null &&
                fileDescriptions.firstOrNull { it?.equals(items[1].fileDescription) == true } != null
        )
    }

    @Ignore("Not working")
    @Test
    fun whenUpdateFileDescription_thenItUpdatedWhenGetIt() {
        val fileDescription = FileDescription(
            "Оператор1 Петровна",
            null,
            2677504,
            Date().time
        ).apply {
            incomingName = "Прихожая_мебель.png"
            downloadPath = "https://mobile4.dev.flex.mfms.ru/files/20231122-f265b137-71ca-4ac1-b01b-aeab884b6d07.png"
            originalPath = "https://mobile4.dev.flex.mfms.ru/files/20231122-f265b137-71ca-4ac1-b01b-aeab884b6d07.png"
            isDownloadError = false
            mimeType = "image/png"
            state = AttachmentStateEnum.READY
            timeStamp = 1700640835225
        }
        val item = ConsultPhrase(
            "ede57de8-832f-49e5-9ae4-886e37a2c895",
            fileDescription = fileDescription,
            modified = ModificationStateEnum.NONE,
            quote = null,
            consultName = "Оператор1 Петровна",
            phraseText = "",
            formattedPhrase = null,
            date = 1700640835225L,
            consultId = "4",
            avatarPath = "20230703-e8c30f10-aa04-417f-bd97-99544fab26f2.jpg",
            read = false,
            status = null,
            sex = false,
            threadId = 455,
            quickReplies = arrayListOf(),
            isBlockInput = false,
            speechStatus = SpeechStatus.NO_SPEECH_STATUS,
            role = ConsultRole.OPERATOR
        )
        database.putChatItems(listOf(item))
        fileDescription.timeStamp = Date().time
        database.updateFileDescription(fileDescription)
        val fileDescriptions = database.getAllFileDescriptions().blockingGet()
        assert(
            fileDescriptions?.firstOrNull { it?.equals(fileDescription) == true } != null
        )
    }

    @Test
    fun whenUpdatingChatItemByTimestamp_thenItUpdatedWhenGet() {
        val time = Date().time
        val message = UserPhrase(
            "2332zone2326",
            "Some text",
            null,
            time,
            null,
            MessageStatus.SENT,
            2330
        )
        database.putChatItem(message)
        message.sentState = MessageStatus.DELIVERED
        database.updateChatItemByTimeStamp(message)
        val itemsInDb = database.getChatItems(0, 1000)
        assert(containsItems(arrayOf(message), itemsInDb.toTypedArray()))
    }

    private fun containsItems(
        itemsToCheck: Array<UserPhrase>,
        itemsFromDatabase: Array<ChatItem>
    ): Boolean {
        val userMessagesListFromDb = itemsFromDatabase.filterIsInstance<UserPhrase>()

        itemsToCheck.forEach { itemToCheck ->
            userMessagesListFromDb.firstOrNull { itemFromDb ->
                itemFromDb.id == itemToCheck.id &&
                    itemFromDb.phraseText == itemToCheck.phraseText &&
                    itemFromDb.threadId == itemToCheck.threadId &&
                    itemFromDb.fileDescription == itemToCheck.fileDescription
            } ?: return false
        }

        return true
    }
}
