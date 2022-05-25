package im.threads.internal.secureDatabase

import im.threads.internal.Config
import im.threads.internal.model.ChatItem
import im.threads.internal.model.ConsultInfo
import im.threads.internal.model.ConsultPhrase
import im.threads.internal.model.FileDescription
import im.threads.internal.model.MessageState
import im.threads.internal.model.SpeechMessageUpdate
import im.threads.internal.model.Survey
import im.threads.internal.model.UserPhrase
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class DatabaseHolder private constructor() {
    /**
     * For Autotests purposes
     *
     * @return MyOpenHelper instance
     */
    val myOpenHelper = ThreadsDbHelper(Config.instance.context)

    fun cleanDatabase() {
        myOpenHelper.cleanDatabase()
    }

    fun getChatItems(offset: Int, limit: Int): List<ChatItem> =
        myOpenHelper.getChatItems(offset, limit)

    fun getChatItem(messageUuid: String?): ChatItem? = myOpenHelper.getChatItem(messageUuid)

    fun putChatItems(items: List<ChatItem?>?) {
        myOpenHelper.putChatItems(items)
    }

    fun putChatItem(chatItem: ChatItem?): Boolean = myOpenHelper.putChatItem(chatItem)

    // FileDescriptions
    val allFileDescriptions: Single<List<FileDescription?>?>
        get() = Single.fromCallable { myOpenHelper.getAllFileDescriptions() }
            .subscribeOn(Schedulers.io())

    // UserPhrase
    fun updateFileDescription(fileDescription: FileDescription) {
        myOpenHelper.updateFileDescription(fileDescription)
    }

    fun getConsultInfo(id: String): ConsultInfo? = myOpenHelper.getLastConsultInfo(id)

    fun getUnsendUserPhrase(count: Int): List<UserPhrase> = myOpenHelper.getUnsendUserPhrase(count)

    // ConsultPhrase
    fun setStateOfUserPhraseByProviderId(providerId: String?, messageState: MessageState?) {
        myOpenHelper.setUserPhraseStateByProviderId(providerId, messageState)
    }

    val lastConsultPhrase: Single<ConsultPhrase?> =
        Single.fromCallable { myOpenHelper.getLastConsultPhrase() }
            .subscribeOn(Schedulers.io())

    fun setAllConsultMessagesWereRead(): Completable {
        return Completable.fromCallable { myOpenHelper.setAllConsultMessagesWereRead() }
            .subscribeOn(Schedulers.io())
    }

    fun setMessageWasRead(uuid: String?) {
        uuid?.let { myOpenHelper.setMessageWasRead(it) }
    }

    fun saveSpeechMessageUpdate(speechMessageUpdate: SpeechMessageUpdate?) {
        myOpenHelper.speechMessageUpdated(speechMessageUpdate)
    }

    fun getSurvey(sendingId: Long): Survey? {
        return myOpenHelper.getSurvey(sendingId)
    }

    fun setNotSentSurveyDisplayMessageToFalse(): Completable {
        return Completable.fromCallable { myOpenHelper.setNotSentSurveyDisplayMessageToFalse() }
            .subscribeOn(Schedulers.io())
    }

    fun setOldRequestResolveThreadDisplayMessageToFalse(): Completable {
        return Completable.fromCallable { myOpenHelper.setOldRequestResolveThreadDisplayMessageToFalse() }
            .subscribeOn(Schedulers.io())
    }

    val messagesCount: Int = myOpenHelper.getMessagesCount()

    val unreadMessagesCount: Int = myOpenHelper.getUnreadMessagesCount()

    val unreadMessagesUuid: List<String?> = myOpenHelper.getUnreadMessagesUuid()

    fun checkAndUpdate() {
        val oldHelper = im.threads.internal.database.ThreadsDbHelper(Config.instance.context)
        if (needMigrateToNewDB(oldHelper)) {
            putChatItems(oldHelper.getChatItems(0, -1))
            myOpenHelper.putFileDescriptions(oldHelper.getAllFileDescriptions())
            oldHelper.cleanDatabase()
        }
    }

    fun needMigrateToNewDB(helper: im.threads.internal.database.ThreadsDbHelper): Boolean {
        return helper.getChatItems(0, -1).size > 0 || helper.getAllFileDescriptions().size > 0
    }

    companion object {

        private var instance: DatabaseHolder? = null

        @JvmStatic
        fun getInstance(): DatabaseHolder {
            if (instance == null) {
                instance = DatabaseHolder()
                instance?.checkAndUpdate()
            }
            return instance!!
        }
        // ChatItems
        /** Nullify instance. For Autotests purposes */
        fun eraseInstance() {
            instance = null
        }
    }
}
