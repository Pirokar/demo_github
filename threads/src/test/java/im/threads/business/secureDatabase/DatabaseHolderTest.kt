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
import im.threads.business.models.RequestResolveThread
import im.threads.business.models.SpeechMessageUpdate
import im.threads.business.models.Survey
import im.threads.business.models.UserPhrase
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.models.enums.ModificationStateEnum
import org.junit.Before
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

    @Test
    fun whenPutConsultInfo_thenItReturnsWhenGet() {
        val item = getConsultPhrase()
        database.putChatItem(item)
        val dbConsult = database.getConsultInfo(item.consultId!!)
        assert(item.consultId == dbConsult!!.id && item.consultName == dbConsult.name)
    }

    @Test
    fun whenPutUnsentPhrase_thenItReturnsWhenGet() {
        val correctItems = arrayOf(
            UserPhrase(
                "2332fvd7023",
                "failed_item",
                null,
                Date().time,
                null,
                MessageStatus.FAILED,
                2323
            ),
            UserPhrase(
                "2332fvd7024",
                "failed_item2",
                null,
                Date().time + 100,
                null,
                MessageStatus.FAILED,
                2324
            )
        )
        val deliveredItem = UserPhrase(
            "2332fvd7788",
            "delivered_item",
            null,
            Date().time + 300,
            null,
            MessageStatus.DELIVERED,
            2326
        )

        database.putChatItems(
            listOf(
                *correctItems,
                deliveredItem
            )
        )
        val unsentItems = database.getUnsentUserPhrase(20)
        assert(containsItems(correctItems, unsentItems.toTypedArray()) && !containsItems(arrayOf(deliveredItem), unsentItems.toTypedArray()))
    }

    @Test
    fun whenSetStateOfUserPhraseByCorrelationId__thenItIsUpdatedWhenGet() {
        val item = UserPhrase(
            "2332fvd8024",
            "failed_item2",
            null,
            Date().time,
            null,
            MessageStatus.FAILED,
            2324
        )
        database.putChatItem(item)
        val newState = MessageStatus.DELIVERED
        database.setStateOfUserPhraseByCorrelationId(item.id, newState)
        val updatedItem = database.getChatItemByCorrelationId(item.id) as UserPhrase
        assert(updatedItem.sentState == newState)
    }

    @Test
    fun whenSetStateOfUserPhraseByBackendMessageId__thenItIsUpdatedWhenGet() {
        val item = UserPhrase(
            "2332fvd8024",
            "failed_item2",
            null,
            Date().time,
            null,
            MessageStatus.FAILED,
            2324
        ).apply {
            backendMessageId = "555what223552"
        }
        database.putChatItem(item)
        val newState = MessageStatus.DELIVERED
        database.setStateOfUserPhraseByBackendMessageId(item.backendMessageId, newState)
        val updatedItem = database.getChatItemByBackendMessageId(item.backendMessageId) as UserPhrase
        assert(updatedItem.sentState == newState)
    }

    @Test
    fun whenSetAllConsultMessagesWereRead_thenAllOfThemAreReadWhenGet() {
        database.putChatItem(getConsultPhrase())
        database.setAllConsultMessagesWereRead().blockingGet()
        val notReadItems = database.getChatItems(0, 400)
            .filterIsInstance<ConsultPhrase>()
            .filter { !it.read }
        assert(notReadItems.isEmpty())
    }

    @Test
    fun whenSetAllConsultMessagesWereReadInThread_thenAllOfThemAreReadWhenGet() {
        val threadId = 800L
        database.putChatItem(getConsultPhrase(threadId = threadId))
        database.setAllConsultMessagesWereReadInThread(threadId).blockingGet()
        val notReadItems = database.getChatItems(0, 400)
            .filterIsInstance<ConsultPhrase>()
            .filter { it.threadId == threadId && !it.read }
        assert(notReadItems.isEmpty())
    }

    @Test
    fun whenSetMessageWasRead_thenItIsReadWhenGetIt() {
        val message = getConsultPhrase(id = "edn57de8-832f-49e5-9ae4-886e37a2c895")
        database.putChatItem(message)
        database.setMessageWasRead(message.id)
        val messageFromDb = database.getChatItemByCorrelationId(message.id) as ConsultPhrase
        assert(messageFromDb.read)
    }

    @Test
    fun whenSaveSpeechMessageUpdate_thenItIsUpdatedWhenGet() {
        val fileDescription = FileDescription(
            "Оператор1 Петровна",
            null,
            2677505,
            Date().time
        ).apply {
            incomingName = "Прихожая_мебель.ogg"
            downloadPath = "https://mobile4.dev.flex.mfms.ru/files/20231122-f265b137-71ca-4ac1-b01b-aeab884b6d07.ogg"
            originalPath = "https://mobile4.dev.flex.mfms.ru/files/20231122-f265b137-71ca-4ac1-b01b-aeab884b6d07.ogg"
            isDownloadError = false
            mimeType = "audio/ogg"
            state = AttachmentStateEnum.READY
            timeStamp = 1700640835333
        }
        val item = getConsultPhrase(
            id = "edna7de8-832f-49e5-9ae4-886e37a2c895",
            speechStatus = SpeechStatus.MAXSPEECH
        )
        database.putChatItem(item)

        val speechMessageUpdate = SpeechMessageUpdate(
            item.id!!,
            SpeechStatus.SUCCESS,
            fileDescription
        )

        database.saveSpeechMessageUpdate(speechMessageUpdate)
        val messageFromDb = database.getChatItemByCorrelationId(item.id) as ConsultPhrase
        assert(messageFromDb.speechStatus == speechMessageUpdate.speechStatus)
    }

    @Test
    fun whenSetNotSentSurveyDisplayMessageToFalse_thenItIsFalseWhenReturns() {
        val survey = Survey(
            "surv7de8-832f-49e5-9ae4-886e37a2c895",
            63636L,
            null,
            Date().time,
            MessageStatus.FAILED,
            read = false,
            displayMessage = true
        )
        database.putChatItem(survey)
        database.setNotSentSurveyDisplayMessageToFalse().blockingGet()
        val dbMessage = database.getChatItemByCorrelationId(survey.uuid) as Survey
        assert(!dbMessage.isDisplayMessage)
    }

    @Test
    fun whenSetOldRequestResolveThreadDisplayMessageToFalse_thenItIsFalseWhenReturns() {
        val requestResolveThread = RequestResolveThread(
            "rero7de8-832f-49e5-9ae4-886e37a2c895",
            60000L,
            Date().time,
            334345600L,
            false
        )
        database.putChatItem(requestResolveThread)
        database.setOldRequestResolveThreadDisplayMessageToFalse().blockingGet()
        val dbMessage = database.getChatItemByCorrelationId(requestResolveThread.uuid)
        assert(dbMessage == null)
    }

    @Test
    fun whenGetMessagesCount_thenItIsCorrect() {
        database.cleanDatabase()
        val messages = listOf(getConsultPhrase(), getConsultPhrase(id = "tes57de8-832f-49e5-9ae4-886e37a2c895"))
        database.putChatItems(messages)
        val itemsCount = database.getMessagesCount()
        assert(itemsCount == messages.size)
    }

    @Test
    fun whenGetUnreadMessagesCount_thenItIsCorrect() {
        database.cleanDatabase()
        val messages = listOf(
            getConsultPhrase(read = true),
            getConsultPhrase(id = "tes57de8-832f-49e5-9ae4-886e37a2c895"),
            getConsultPhrase(id = "ees57de8-832f-49e5-9ae4-886e37a2c895", read = true)
        )
        database.putChatItems(messages)
        val itemsCount = database.getUnreadMessagesCount()
        assert(itemsCount == 1)
    }

    @Test
    fun whenGetUnreadMessagesUuid_thenItIsCorrect() {
        database.cleanDatabase()
        val messages = listOf(
            getConsultPhrase(),
            getConsultPhrase(id = "tes57de8-832f-49e5-9ae4-886e37a2c895", read = true),
            getConsultPhrase(id = "ees57de8-832f-49e5-9ae4-886e37a2c895")
        )
        val unreadUuids = messages.map { it.id }
        database.putChatItems(messages)
        val unreadUuidsInDb = database.getUnreadMessagesUuid()
        assert(unreadUuidsInDb.size == 2 && unreadUuidsInDb.any { it in unreadUuids })
    }

    @Test
    fun whenSetOrUpdateMessageId_thenItIsUpdatedWhenGet() {
        val message = UserPhrase(
            "2332fvd5656",
            "sent_item",
            null,
            Date().time,
            null,
            MessageStatus.SENT,
            2323
        )
        database.putChatItem(message)
        message.backendMessageId = "555where223552"
        database.setOrUpdateMessageId(message.id, message.backendMessageId)
        val messageInDb = database.getChatItemByBackendMessageId(message.backendMessageId)
        assert(messageInDb != null)
    }

    @Test
    fun whenRemovingItemWithCorrelationId_thenWhenGetReturnsNull() {
        val message = getConsultPhrase(id = "rmv57de8-832f-49e5-9ae4-886e37a2c895")
        database.putChatItem(message)
        database.removeItem(message.id, null)
        val messageInDb = database.getChatItemByCorrelationId(message.id)
        assert(messageInDb == null)
    }

    @Test
    fun whenRemovingItemWithBackendId_thenWhenGetReturnsNull() {
        val message = UserPhrase(
            "2332fvd5659",
            "delivered_item",
            null,
            Date().time,
            null,
            MessageStatus.DELIVERED,
            2329
        ).apply {
            backendMessageId = "555remove223552"
        }
        database.putChatItem(message)
        database.removeItem(null, message.backendMessageId)
        val messageInDb = database.getChatItemByBackendMessageId(message.backendMessageId)
        assert(messageInDb == null)
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

    private fun getConsultPhrase(
        fileDescription: FileDescription? = null,
        id: String = "ede57de8-832f-49e5-9ae4-886e37a2c895",
        threadId: Long = 455,
        speechStatus: SpeechStatus = SpeechStatus.NO_SPEECH_STATUS,
        read: Boolean = false
    ) = ConsultPhrase(
        id = id,
        fileDescription = fileDescription,
        modified = ModificationStateEnum.NONE,
        quote = null,
        consultName = "Оператор1 Петровна",
        phraseText = "",
        formattedPhrase = null,
        date = 1700640835225L,
        consultId = "4",
        avatarPath = "20230703-e8c30f10-aa04-417f-bd97-99544fab26f2.jpg",
        read = read,
        status = null,
        sex = false,
        threadId = threadId,
        quickReplies = arrayListOf(),
        isBlockInput = false,
        speechStatus = speechStatus,
        role = ConsultRole.OPERATOR
    )
}