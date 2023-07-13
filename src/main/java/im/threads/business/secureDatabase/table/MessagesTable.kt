package im.threads.business.secureDatabase.table

import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import im.threads.business.config.BaseConfig
import im.threads.business.formatters.SpeechStatus.Companion.fromString
import im.threads.business.logger.LoggerEdna
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultConnectionMessage
import im.threads.business.models.ConsultInfo
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.ConsultRole
import im.threads.business.models.FileDescription
import im.threads.business.models.MessageStatus
import im.threads.business.models.RequestResolveThread
import im.threads.business.models.SimpleSystemMessage
import im.threads.business.models.SpeechMessageUpdate
import im.threads.business.models.Survey
import im.threads.business.models.UserPhrase
import im.threads.business.utils.FileDownloader.Companion.getDownloadDir
import im.threads.business.utils.FileProvider
import im.threads.business.utils.FileUtils.generateFileName
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteOpenHelper
import java.io.File
import java.util.Locale

class MessagesTable(
    private val fileDescriptionTable: FileDescriptionsTable,
    private val quotesTable: QuotesTable,
    private val quickRepliesTable: QuickRepliesTable,
    private val questionsTable: QuestionsTable
) : Table() {

    override fun createTable(db: SQLiteDatabase) {
        db.execSQL(
            String.format(
                Locale.US,
                "create table %s " + // messages table
                    "( %s integer primary key autoincrement," + // id column
                    " %s integer, " + // timestamp
                    " %s text, " + // phrase
                    " %s text, " + // COLUMN_FORMATTED_PHRASE
                    " %s integer, " + // item type
                    " %s text, " + // name
                    " %s text, " + // avatar path
                    " %s text, " + // correlation message id
                    " %s text, " + // message id (received from backend)
                    " %s integer, " + // sex
                    " %s integer," + // message sent state
                    "%s text," + // consultid
                    "%s text," + // COLUMN_CONSULT_STATUS
                    "%s text" + // COLUMN_CONSULT_TITLE
                    ", " + COLUMN_CONSULT_ORG_UNIT + " text" + // COLUMN_CONSULT_ORG_UNIT
                    ", " + COLUMN_CONSULT_ROLE + " text" + // COLUMN_CONSULT_ROLE
                    ", " + "%s text," + // connection type
                    "%s integer" + // isRead
                    ", " + COLUMN_DISPLAY_MESSAGE + " integer" +
                    ", " + COLUMN_SURVEY_SENDING_ID + " integer" +
                    ", " + COLUMN_SURVEY_HIDE_AFTER + " integer" +
                    ", " + COLUMN_THREAD_ID + " integer" +
                    ", " + COLUMN_BLOCK_INPUT + " integer" +
                    ", " + COLUMN_SPEECH_STATUS + " text" +
                    ")",
                TABLE_MESSAGES,
                COLUMN_TABLE_ID,
                COLUMN_TIMESTAMP,
                COLUMN_PHRASE,
                COLUMN_FORMATTED_PHRASE,
                COLUMN_MESSAGE_TYPE,
                COLUMN_NAME,
                COLUMN_AVATAR_PATH,
                COLUMN_MESSAGE_CORRELATION_ID,
                COLUMN_MESSAGE_ID,
                COLUMN_SEX,
                COLUMN_MESSAGE_SEND_STATE,
                COLUMN_CONSULT_ID,
                COLUMN_CONSULT_STATUS,
                COLUMN_CONSULT_TITLE,
                COLUMN_CONNECTION_TYPE,
                COLUMN_IS_READ
            )
        )
    }

    override fun upgradeTable(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_MESSAGES")
        }
    }

    override fun cleanTable(sqlHelper: SQLiteOpenHelper) {
        sqlHelper.writableDatabase.execSQL("delete from $TABLE_MESSAGES")
    }

    fun getChatItems(sqlHelper: SQLiteOpenHelper, offset: Int, limit: Int): List<ChatItem> {
        val items: MutableList<ChatItem> = ArrayList()
        val query = String.format(
            Locale.US,
            "select * from (select * from %s order by %s desc limit %s offset %s) order by %s asc",
            TABLE_MESSAGES,
            COLUMN_TIMESTAMP,
            limit,
            offset,
            COLUMN_TIMESTAMP
        )
        sqlHelper.readableDatabase.rawQuery(query, null).use { c ->
            if (c.count == 0) {
                return items
            }
            c.moveToFirst()
            while (!c.isAfterLast) {
                val chatItem: ChatItem? = getChatItemByCorrelationId(sqlHelper, c)
                if (chatItem != null) {
                    items.add(chatItem)
                }
                c.moveToNext()
            }
        }
        return items
    }

    fun getSendingChatItems(sqlHelper: SQLiteOpenHelper): List<UserPhrase> {
        val items: MutableList<UserPhrase> = ArrayList()
        val query = String.format(
            Locale.US,
            "select * from (select * from %s " +
                " where " + COLUMN_MESSAGE_SEND_STATE + " = " + MessageStatus.SENDING.value +
                " order by %s desc) order by %s asc",
            TABLE_MESSAGES,
            COLUMN_TIMESTAMP,
            COLUMN_TIMESTAMP
        )
        sqlHelper.readableDatabase.rawQuery(query, arrayOf()).use { c ->
            if (c.moveToFirst()) {
                while (!c.isAfterLast) {
                    val chatItem: UserPhrase = getUserPhrase(sqlHelper, c)
                    items.add(chatItem)
                    c.moveToNext()
                }
            }
        }
        return items
    }

    fun getNotDeliveredChatItems(sqlHelper: SQLiteOpenHelper): List<UserPhrase> {
        val items: MutableList<UserPhrase> = ArrayList()
        val query = String.format(
            Locale.US,
            "select * from (select * from %s " +
                " where " + COLUMN_MESSAGE_SEND_STATE + " < " + MessageStatus.DELIVERED.value +
                " order by %s desc) order by %s asc",
            TABLE_MESSAGES,
            COLUMN_TIMESTAMP,
            COLUMN_TIMESTAMP
        )
        sqlHelper.readableDatabase.rawQuery(query, arrayOf()).use { c ->
            if (c.moveToFirst()) {
                while (!c.isAfterLast) {
                    val chatItem: UserPhrase = getUserPhrase(sqlHelper, c)
                    items.add(chatItem)
                    c.moveToNext()
                }
            }
        }
        return items
    }

    fun getChatItemByCorrelationId(sqlHelper: SQLiteOpenHelper, messageUuid: String?): ChatItem? {
        val sql = (
            "select * from " + TABLE_MESSAGES +
                " where " + COLUMN_MESSAGE_CORRELATION_ID + " = ?" +
                " order by " + COLUMN_TIMESTAMP + " desc"
            )
        val selectionArgs = arrayOf(messageUuid)
        sqlHelper.readableDatabase.rawQuery(sql, selectionArgs).use { c ->
            if (c.moveToFirst()) {
                return getChatItemByCorrelationId(sqlHelper, c)
            }
        }
        return null
    }

    fun getChatItemByBackendMessageId(sqlHelper: SQLiteOpenHelper, messageId: String?): ChatItem? {
        if (messageId == null) return null

        val sql = (
            "select * from " + TABLE_MESSAGES +
                " where " + COLUMN_MESSAGE_ID + " = '$messageId'" +
                " order by " + COLUMN_TIMESTAMP + " desc"
            )
        sqlHelper.readableDatabase.rawQuery(sql, null).use { c ->
            if (c.moveToFirst()) {
                return getChatItemByCorrelationId(sqlHelper, c)
            }
        }
        return null
    }

    fun putChatItems(sqlHelper: SQLiteOpenHelper, chatItems: List<ChatItem?>?) {
        try {
            sqlHelper.writableDatabase.beginTransaction()
            if (chatItems != null) {
                for (item: ChatItem? in chatItems) {
                    putChatItem(sqlHelper, item)
                }
            }
            sqlHelper.writableDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            LoggerEdna.error("putMessagesSync", e)
        } finally {
            sqlHelper.writableDatabase.endTransaction()
        }
    }

    fun putChatItem(sqlHelper: SQLiteOpenHelper, chatItem: ChatItem?): Boolean {
        if (chatItem is ConsultConnectionMessage) {
            insertOrUpdateMessage(sqlHelper, getConsultConnectionMessageCV(chatItem))
            return true
        }
        if (chatItem is SimpleSystemMessage && !chatItem.uuid.isNullOrEmpty()) {
            insertOrUpdateMessage(sqlHelper, getSimpleSystemMessageCV(chatItem))
            return true
        }
        if (chatItem is ConsultPhrase) {
            val item = getChatItemByCorrelationId(sqlHelper, chatItem.id) as? ConsultPhrase
            chatItem.read = if (item?.read == true) true else chatItem.read
            insertOrUpdateMessage(sqlHelper, getConsultPhraseCV(chatItem))
            chatItem.fileDescription?.let {
                isFileDownloaded(it)?.let { uri ->
                    setProgressAndFileUri(it, 100, uri)
                }
                fileDescriptionTable.putFileDescription(
                    sqlHelper,
                    it,
                    chatItem.id.orEmpty(),
                    false
                )
            }
            if (chatItem.quote != null) {
                chatItem.id?.let { quotesTable.putQuote(sqlHelper, it, chatItem.quote) }
            }
            if (chatItem.quickReplies != null) {
                chatItem.id?.let {
                    quickRepliesTable.putQuickReplies(sqlHelper, it, chatItem.quickReplies)
                }
            }
            return true
        }
        if (chatItem is UserPhrase) {
            chatItem.id?.let { id ->
                if (chatItem.sentState != MessageStatus.READ) {
                    val existedItem = getChatItemByCorrelationId(sqlHelper, id) as? UserPhrase
                    if (existedItem != null && existedItem.sentState.value > chatItem.sentState.value) {
                        chatItem.sentState = existedItem.sentState
                    }
                }

                insertOrUpdateMessage(sqlHelper, getUserPhraseCV(chatItem))
                chatItem.fileDescription?.let {
                    isFileDownloaded(it)?.let { uri ->
                        setProgressAndFileUri(it, 100, uri)
                    }
                    fileDescriptionTable.putFileDescription(
                        sqlHelper,
                        it,
                        chatItem.id.orEmpty(),
                        false
                    )
                }
                chatItem.quote?.let { quote -> quotesTable.putQuote(sqlHelper, id, quote) }
            } ?: run {
                chatItem.fileDescription?.originalPath?.let {
                    fileDescriptionTable.updateFileDescriptionByUrl(
                        sqlHelper,
                        chatItem.fileDescription!!
                    )
                }
            }

            return true
        }
        if (chatItem is Survey) {
            setNotSentSurveyDisplayMessageToFalse(sqlHelper, chatItem.sendingId)
            insertOrUpdateSurvey(sqlHelper, chatItem)
        }
        if (chatItem is RequestResolveThread) {
            setOldRequestResolveThreadDisplayMessageToFalse(sqlHelper, chatItem.uuid ?: "")
            insertOrUpdateMessage(sqlHelper, getRequestResolveThreadCV(chatItem))
            return true
        }
        return false
    }

    fun getLastConsultInfo(sqlHelper: SQLiteOpenHelper, id: String): ConsultInfo? {
        val sql =
            (
                "select " + COLUMN_AVATAR_PATH + ", " + COLUMN_NAME + ", " + COLUMN_CONSULT_STATUS +
                    " from " + TABLE_MESSAGES +
                    " where " + COLUMN_CONSULT_ID + " =  ? " +
                    " order by " + COLUMN_TIMESTAMP + " desc"
                )
        val selectionArgs = arrayOf(id)
        sqlHelper.readableDatabase.rawQuery(sql, selectionArgs).use { c ->
            if (c.moveToFirst()) {
                return ConsultInfo(
                    cursorGetString(c, COLUMN_NAME),
                    id,
                    cursorGetString(c, COLUMN_CONSULT_STATUS),
                    cursorGetString(c, COLUMN_CONSULT_ORG_UNIT),
                    cursorGetString(c, COLUMN_AVATAR_PATH),
                    cursorGetString(c, COLUMN_CONSULT_ROLE)
                )
            }
        }
        return null
    }

    fun getUnsendUserPhrase(sqlHelper: SQLiteOpenHelper, count: Int): List<UserPhrase> {
        val userPhrases: MutableList<UserPhrase> = ArrayList()
        val chatItems = getChatItems(sqlHelper, 0, count)
        for (chatItem: ChatItem? in chatItems) {
            if (chatItem is UserPhrase) {
                if (chatItem.sentState == MessageStatus.FAILED) {
                    userPhrases.add(chatItem)
                }
            }
        }
        return userPhrases
    }

    fun setUserPhraseStateByCorrelationId(
        sqlHelper: SQLiteOpenHelper,
        uuid: String?,
        messageStatus: MessageStatus?
    ) {
        val cv = ContentValues()
        cv.put(COLUMN_MESSAGE_SEND_STATE, messageStatus?.value)
        sqlHelper.writableDatabase.update(TABLE_MESSAGES, cv, "$COLUMN_MESSAGE_CORRELATION_ID = ?", arrayOf(uuid))
    }

    fun setUserPhraseStateByBackendMessageId(
        sqlHelper: SQLiteOpenHelper,
        messageId: String?,
        messageStatus: MessageStatus?
    ) {
        val cv = ContentValues()
        cv.put(COLUMN_MESSAGE_SEND_STATE, messageStatus?.value)
        sqlHelper.writableDatabase.update(TABLE_MESSAGES, cv, "$COLUMN_MESSAGE_ID = ?", arrayOf(messageId))
    }

    fun getLastConsultPhrase(sqlHelper: SQLiteOpenHelper): ConsultPhrase? {
        val sql = (
            "select * from " + TABLE_MESSAGES +
                " where " + COLUMN_MESSAGE_TYPE + " = " + MessageType.CONSULT_PHRASE.ordinal +
                " order by " + COLUMN_TIMESTAMP + " desc"
            )
        sqlHelper.readableDatabase.rawQuery(sql, arrayOf()).use { c ->
            if (c.moveToFirst()) {
                return getConsultPhrase(sqlHelper, c)
            }
        }
        return null
    }

    fun setAllMessagesWereRead(sqlHelper: SQLiteOpenHelper): Int {
        val cv = ContentValues()
        cv.put(COLUMN_IS_READ, true)
        val whereClause = (
            "(" + COLUMN_MESSAGE_TYPE + " = " + MessageType.CONSULT_PHRASE.ordinal +
                " or (" + COLUMN_MESSAGE_TYPE + " = " + MessageType.SURVEY.ordinal + " and " + COLUMN_MESSAGE_SEND_STATE + " = " + MessageStatus.FAILED.value + ")" +
                " or " + COLUMN_MESSAGE_TYPE + " = " + MessageType.REQUEST_RESOLVE_THREAD.ordinal + ")" +
                " and " + COLUMN_IS_READ + " = 0"
            )
        return sqlHelper.writableDatabase
            .update(TABLE_MESSAGES, cv, whereClause, null)
    }

    fun setAllMessagesWereReadInThread(sqlHelper: SQLiteOpenHelper, threadId: Long): Int {
        val cv = ContentValues()
        cv.put(COLUMN_IS_READ, true)
        val whereClause = (
            "(" + COLUMN_MESSAGE_TYPE + " = " + MessageType.CONSULT_PHRASE.ordinal +
                " or (" + COLUMN_MESSAGE_TYPE + " = " + MessageType.SURVEY.ordinal + " and " + COLUMN_MESSAGE_SEND_STATE + " = " + MessageStatus.FAILED.value + ")" +
                " or " + COLUMN_MESSAGE_TYPE + " = " + MessageType.REQUEST_RESOLVE_THREAD.ordinal + ")" +
                " and " + COLUMN_IS_READ + " = 0 and $COLUMN_THREAD_ID = $threadId"
            )
        return sqlHelper.writableDatabase
            .update(TABLE_MESSAGES, cv, whereClause, null)
    }

    fun setMessageWasRead(sqlHelper: SQLiteOpenHelper, uuid: String) {
        val cv = ContentValues()
        cv.put(COLUMN_IS_READ, true)
        val whereClause = (
            COLUMN_MESSAGE_CORRELATION_ID + " = ? " +
                " and " + COLUMN_IS_READ + " = 0"
            )
        sqlHelper.writableDatabase
            .update(TABLE_MESSAGES, cv, whereClause, arrayOf(uuid))
    }

    fun getSurvey(sqlHelper: SQLiteOpenHelper, sendingId: Long): Survey? {
        val sql = (
            "select * from " + TABLE_MESSAGES +
                " where " + COLUMN_SURVEY_SENDING_ID + " = ?" +
                " order by " + COLUMN_TIMESTAMP + " desc"
            )
        val selectionArgs = arrayOf(sendingId.toString())
        sqlHelper.readableDatabase.rawQuery(sql, selectionArgs).use { c ->
            if (c.moveToFirst()) {
                return getSurvey(sqlHelper, c)
            }
        }
        return null
    }

    fun getMessagesCount(sqlHelper: SQLiteOpenHelper): Int {
        sqlHelper.readableDatabase.rawQuery(
            String.format(
                Locale.US,
                "select count(%s) from %s",
                COLUMN_TABLE_ID,
                TABLE_MESSAGES
            ),
            null
        ).use { c ->
            if (c.count == 0) {
                return 0
            }
            c.moveToFirst()
            return c.getInt(0)
        }
    }

    fun getUnreadMessagesCount(sqlHelper: SQLiteOpenHelper): Int {
        val sql = (
            "select " + COLUMN_MESSAGE_CORRELATION_ID +
                " from " + TABLE_MESSAGES +
                " where (" +
                COLUMN_MESSAGE_TYPE + " = " + MessageType.CONSULT_PHRASE.ordinal + " or " +
                "(" + COLUMN_MESSAGE_TYPE + " = " + MessageType.SURVEY.ordinal + " and " + COLUMN_MESSAGE_SEND_STATE + " = " + MessageStatus.FAILED.value + ") or " +
                COLUMN_MESSAGE_TYPE + " = " + MessageType.REQUEST_RESOLVE_THREAD.ordinal +
                ")" +
                " and " + COLUMN_IS_READ + " = 0" +
                " order by " + COLUMN_TIMESTAMP + " asc"
            )
        sqlHelper.readableDatabase.rawQuery(sql, null).use { c -> return c.count }
    }

    fun updateChatItemByTimeStamp(sqlHelper: SQLiteOpenHelper, chatItem: ChatItem) {
        if (chatItem is UserPhrase) {
            insertOrUpdateMessageByTimeStamp(sqlHelper, getUserPhraseCV(chatItem))
            chatItem.fileDescription?.let {
                isFileDownloaded(it)?.let { uri ->
                    setProgressAndFileUri(it, 100, uri)
                }
                fileDescriptionTable.putFileDescription(
                    sqlHelper,
                    it,
                    chatItem.id.orEmpty(),
                    false
                )
            }
            chatItem.id?.let {
                chatItem.quote?.let { quote -> quotesTable.putQuote(sqlHelper, it, quote) }
            }
        }
    }

    fun getUnreadMessagesUuid(sqlHelper: SQLiteOpenHelper): List<String?> {
        val sql = (
            "select " + COLUMN_MESSAGE_CORRELATION_ID +
                " from " + TABLE_MESSAGES +
                " where (" +
                COLUMN_MESSAGE_TYPE + " = " + MessageType.CONSULT_PHRASE.ordinal + " or " +
                "(" + COLUMN_MESSAGE_TYPE + " = " + MessageType.SURVEY.ordinal + " and " + COLUMN_MESSAGE_SEND_STATE + " = " + MessageStatus.FAILED.value + ") or " +
                COLUMN_MESSAGE_TYPE + " = " + MessageType.REQUEST_RESOLVE_THREAD.ordinal +
                ")" +
                " and " + COLUMN_IS_READ + " = 0" +
                " order by " + COLUMN_TIMESTAMP + " asc"
            )
        val ids: MutableSet<String?> = HashSet()
        sqlHelper.readableDatabase.rawQuery(sql, null).use { c ->
            c.moveToFirst()
            while (!c.isAfterLast) {
                ids.add(cursorGetString(c, COLUMN_MESSAGE_CORRELATION_ID))
                c.moveToNext()
            }
        }
        return ArrayList(ids)
    }

    fun setNotSentSurveyDisplayMessageToFalse(sqlHelper: SQLiteOpenHelper): Int {
        val cv = ContentValues()
        cv.put(COLUMN_DISPLAY_MESSAGE, false)
        val whereClause = (
            COLUMN_MESSAGE_TYPE + " = " + MessageType.SURVEY.ordinal +
                " and " + COLUMN_MESSAGE_SEND_STATE + " = " + MessageStatus.FAILED.value
            )
        return sqlHelper.writableDatabase
            .update(TABLE_MESSAGES, cv, whereClause, null)
    }

    fun setOldRequestResolveThreadDisplayMessageToFalse(sqlHelper: SQLiteOpenHelper): Int {
        val cv = ContentValues()
        cv.put(COLUMN_DISPLAY_MESSAGE, false)
        val whereClause = COLUMN_MESSAGE_TYPE + " = " + MessageType.REQUEST_RESOLVE_THREAD.ordinal
        return sqlHelper.writableDatabase
            .update(TABLE_MESSAGES, cv, whereClause, null)
    }

    fun speechMessageUpdated(
        sqlHelper: SQLiteOpenHelper,
        speechMessageUpdate: SpeechMessageUpdate
    ) {
        val cv = ContentValues()
        val uuid = speechMessageUpdate.uuid
        cv.put(COLUMN_SPEECH_STATUS, speechMessageUpdate.speechStatus.toString())
        fileDescriptionTable.putFileDescription(
            sqlHelper,
            speechMessageUpdate.fileDescription,
            speechMessageUpdate.uuid,
            false
        )
        val whereClause = "$COLUMN_MESSAGE_CORRELATION_ID = ?"
        sqlHelper.writableDatabase
            .update(TABLE_MESSAGES, cv, whereClause, arrayOf(uuid))
    }

    fun setOrUpdateMessageId(sqlHelper: SQLiteOpenHelper, correlationId: String?, messageId: String?) {
        if (correlationId != null && messageId != null) {
            val content = ContentValues().apply {
                put(COLUMN_MESSAGE_ID, messageId)
            }
            val split = correlationId.split(":")
            val correlationIdWithHandle = if (split.size > 1) {
                split[1]
            } else {
                split[0]
            }
            val whereClause = "$COLUMN_MESSAGE_CORRELATION_ID = '$correlationIdWithHandle'"
            sqlHelper.writableDatabase.update(TABLE_MESSAGES, content, whereClause, null)
        }
    }

    fun removeItem(sqlHelper: SQLiteOpenHelper, correlationId: String?, messageId: String?) {
        val removedItemCount = if (correlationId != null) {
            sqlHelper.writableDatabase.delete(TABLE_MESSAGES, "$COLUMN_MESSAGE_CORRELATION_ID = '$correlationId'", null)
        } else {
            0
        }

        if (removedItemCount == 0 && messageId != null) {
            sqlHelper.writableDatabase.delete(TABLE_MESSAGES, "$COLUMN_MESSAGE_ID = '$messageId'", null)
        }
    }

    private fun getChatItemByCorrelationId(sqlHelper: SQLiteOpenHelper, c: Cursor): ChatItem? {
        when (cursorGetInt(c, COLUMN_MESSAGE_TYPE)) {
            MessageType.CONSULT_CONNECTED.ordinal -> {
                return ConsultConnectionMessage(
                    cursorGetString(c, COLUMN_MESSAGE_CORRELATION_ID),
                    cursorGetString(c, COLUMN_CONSULT_ID),
                    cursorGetString(c, COLUMN_CONNECTION_TYPE),
                    cursorGetString(c, COLUMN_NAME),
                    cursorGetBool(c, COLUMN_SEX),
                    cursorGetLong(c, COLUMN_TIMESTAMP),
                    cursorGetString(c, COLUMN_AVATAR_PATH),
                    cursorGetString(c, COLUMN_CONSULT_STATUS),
                    cursorGetString(c, COLUMN_CONSULT_TITLE),
                    cursorGetString(c, COLUMN_CONSULT_ORG_UNIT),
                    cursorGetString(c, COLUMN_CONSULT_ROLE),
                    cursorGetBool(c, COLUMN_DISPLAY_MESSAGE),
                    cursorGetString(c, COLUMN_PHRASE),
                    cursorGetLong(c, COLUMN_THREAD_ID)
                )
            }
            MessageType.SYSTEM_MESSAGE.ordinal -> {
                return SimpleSystemMessage(
                    cursorGetString(c, COLUMN_MESSAGE_CORRELATION_ID),
                    cursorGetString(c, COLUMN_CONNECTION_TYPE),
                    cursorGetLong(c, COLUMN_TIMESTAMP),
                    cursorGetString(c, COLUMN_PHRASE),
                    cursorGetLong(c, COLUMN_THREAD_ID)
                )
            }
            MessageType.CONSULT_PHRASE.ordinal -> {
                return getConsultPhrase(sqlHelper, c)
            }
            MessageType.USER_PHRASE.ordinal -> {
                return getUserPhrase(sqlHelper, c)
            }
            MessageType.SURVEY.ordinal -> {
                return getSurvey(sqlHelper, c)
            }
            MessageType.REQUEST_RESOLVE_THREAD.ordinal -> {
                return getRequestResolveThread(c)
            }
            else -> return null
        }
    }

    private fun getConsultPhrase(sqlHelper: SQLiteOpenHelper, c: Cursor): ConsultPhrase {
        return ConsultPhrase(
            cursorGetString(c, COLUMN_MESSAGE_CORRELATION_ID),
            fileDescriptionTable.getFileDescription(
                sqlHelper,
                cursorGetString(c, COLUMN_MESSAGE_CORRELATION_ID)
            ),
            quotesTable.getQuote(sqlHelper, cursorGetString(c, COLUMN_MESSAGE_CORRELATION_ID)),
            cursorGetString(c, COLUMN_NAME),
            cursorGetString(c, COLUMN_PHRASE),
            cursorGetString(c, COLUMN_FORMATTED_PHRASE),
            cursorGetLong(c, COLUMN_TIMESTAMP),
            cursorGetString(c, COLUMN_CONSULT_ID),
            cursorGetString(c, COLUMN_AVATAR_PATH),
            cursorGetBool(c, COLUMN_IS_READ),
            cursorGetString(c, COLUMN_CONSULT_STATUS),
            cursorGetBool(c, COLUMN_SEX),
            cursorGetLong(c, COLUMN_THREAD_ID),
            cursorGetString(c, COLUMN_MESSAGE_CORRELATION_ID)?.let {
                quickRepliesTable.getQuickReplies(sqlHelper, it)
            },
            cursorGetBool(c, COLUMN_BLOCK_INPUT),
            fromString(cursorGetString(c, COLUMN_SPEECH_STATUS)),
            ConsultRole.consultRoleFromString(cursorGetString(c, COLUMN_CONSULT_ROLE))
        )
    }

    private fun getUserPhrase(sqlHelper: SQLiteOpenHelper, c: Cursor): UserPhrase {
        return UserPhrase(
            cursorGetString(c, COLUMN_MESSAGE_CORRELATION_ID),
            cursorGetString(c, COLUMN_PHRASE),
            quotesTable.getQuote(sqlHelper, cursorGetString(c, COLUMN_MESSAGE_CORRELATION_ID)),
            cursorGetLong(c, COLUMN_TIMESTAMP),
            fileDescriptionTable.getFileDescription(
                sqlHelper,
                cursorGetString(c, COLUMN_MESSAGE_CORRELATION_ID)
            ),
            MessageStatus.fromOrdinal(cursorGetInt(c, COLUMN_MESSAGE_SEND_STATE)),
            cursorGetLong(c, COLUMN_THREAD_ID)
        )
    }

    private fun getSurvey(sqlHelper: SQLiteOpenHelper, c: Cursor): Survey {
        val surveySendingId = cursorGetLong(c, COLUMN_SURVEY_SENDING_ID)
        val survey = Survey(
            cursorGetString(c, COLUMN_MESSAGE_CORRELATION_ID),
            surveySendingId,
            cursorGetLong(c, COLUMN_SURVEY_HIDE_AFTER),
            cursorGetLong(c, COLUMN_TIMESTAMP),
            MessageStatus.fromOrdinal(cursorGetInt(c, COLUMN_MESSAGE_SEND_STATE)),
            cursorGetBool(c, COLUMN_IS_READ),
            cursorGetBool(c, COLUMN_DISPLAY_MESSAGE)
        )
        survey.questions = ArrayList(questionsTable.getQuestions(sqlHelper, surveySendingId))
        return survey
    }

    private fun getRequestResolveThread(c: Cursor): RequestResolveThread? {
        val requestResolveThread =
            RequestResolveThread(
                cursorGetString(c, COLUMN_MESSAGE_CORRELATION_ID),
                cursorGetLong(c, COLUMN_SURVEY_HIDE_AFTER),
                cursorGetLong(c, COLUMN_TIMESTAMP),
                cursorGetLong(c, COLUMN_THREAD_ID),
                cursorGetBool(c, COLUMN_IS_READ)
            )
        return if (!cursorGetBool(c, COLUMN_DISPLAY_MESSAGE)) {
            null
        } else {
            requestResolveThread
        }
    }

    private fun getConsultPhraseCV(phrase: ConsultPhrase): ContentValues {
        val cv = ContentValues()
        cv.put(COLUMN_MESSAGE_CORRELATION_ID, phrase.id)
        cv.put(COLUMN_PHRASE, phrase.phraseText)
        cv.put(COLUMN_FORMATTED_PHRASE, phrase.formattedPhrase)
        cv.put(COLUMN_TIMESTAMP, phrase.timeStamp)
        cv.put(COLUMN_MESSAGE_TYPE, MessageType.CONSULT_PHRASE.ordinal)
        cv.put(COLUMN_AVATAR_PATH, phrase.avatarPath)
        cv.put(COLUMN_CONSULT_ID, phrase.consultId)
        cv.put(COLUMN_IS_READ, phrase.read)
        cv.put(COLUMN_CONSULT_STATUS, phrase.status)
        cv.put(COLUMN_NAME, phrase.consultName)
        cv.put(COLUMN_SEX, phrase.sex)
        cv.put(COLUMN_THREAD_ID, phrase.threadId)
        cv.put(COLUMN_BLOCK_INPUT, phrase.isBlockInput)
        cv.put(COLUMN_SPEECH_STATUS, phrase.speechStatus.toString())
        cv.put(COLUMN_CONSULT_ROLE, phrase.role.name)
        return cv
    }

    private fun getUserPhraseCV(phrase: UserPhrase): ContentValues {
        val cv = ContentValues()
        cv.put(COLUMN_MESSAGE_CORRELATION_ID, phrase.id)
        cv.put(COLUMN_PHRASE, phrase.phraseText)
        cv.put(COLUMN_TIMESTAMP, phrase.timeStamp)
        cv.put(COLUMN_MESSAGE_TYPE, MessageType.USER_PHRASE.ordinal)
        cv.put(COLUMN_MESSAGE_SEND_STATE, phrase.sentState.value)
        cv.put(COLUMN_THREAD_ID, phrase.threadId)
        return cv
    }

    private fun getConsultConnectionMessageCV(consultConnectionMessage: ConsultConnectionMessage): ContentValues {
        val cv = ContentValues()
        cv.put(COLUMN_NAME, consultConnectionMessage.name)
        cv.put(COLUMN_TIMESTAMP, consultConnectionMessage.timeStamp)
        cv.put(COLUMN_AVATAR_PATH, consultConnectionMessage.avatarPath)
        cv.put(COLUMN_MESSAGE_TYPE, MessageType.CONSULT_CONNECTED.ordinal)
        cv.put(COLUMN_SEX, consultConnectionMessage.sex)
        cv.put(COLUMN_CONNECTION_TYPE, consultConnectionMessage.connectionType)
        cv.put(COLUMN_CONSULT_ID, consultConnectionMessage.consultId)
        cv.put(COLUMN_CONSULT_STATUS, consultConnectionMessage.status)
        cv.put(COLUMN_CONSULT_TITLE, consultConnectionMessage.title)
        cv.put(COLUMN_CONSULT_ORG_UNIT, consultConnectionMessage.orgUnit)
        cv.put(COLUMN_CONSULT_ROLE, consultConnectionMessage.role)
        cv.put(COLUMN_MESSAGE_CORRELATION_ID, consultConnectionMessage.uuid)
        cv.put(COLUMN_DISPLAY_MESSAGE, consultConnectionMessage.isDisplayMessage)
        cv.put(COLUMN_PHRASE, consultConnectionMessage.getText())
        cv.put(COLUMN_THREAD_ID, consultConnectionMessage.threadId)
        return cv
    }

    private fun getSimpleSystemMessageCV(simpleSystemMessage: SimpleSystemMessage): ContentValues {
        val cv = ContentValues()
        cv.put(COLUMN_MESSAGE_CORRELATION_ID, simpleSystemMessage.uuid)
        cv.put(COLUMN_MESSAGE_TYPE, MessageType.SYSTEM_MESSAGE.ordinal)
        cv.put(COLUMN_CONNECTION_TYPE, simpleSystemMessage.getType())
        cv.put(COLUMN_TIMESTAMP, simpleSystemMessage.timeStamp)
        cv.put(COLUMN_PHRASE, simpleSystemMessage.getText())
        cv.put(COLUMN_THREAD_ID, simpleSystemMessage.threadId)
        return cv
    }

    private fun getRequestResolveThreadCV(requestResolveThread: RequestResolveThread): ContentValues {
        val cv = ContentValues()
        cv.put(COLUMN_MESSAGE_CORRELATION_ID, requestResolveThread.uuid)
        cv.put(COLUMN_MESSAGE_TYPE, MessageType.REQUEST_RESOLVE_THREAD.ordinal)
        cv.put(COLUMN_SURVEY_HIDE_AFTER, requestResolveThread.hideAfter)
        cv.put(COLUMN_TIMESTAMP, requestResolveThread.timeStamp)
        cv.put(COLUMN_THREAD_ID, requestResolveThread.threadId)
        cv.put(COLUMN_DISPLAY_MESSAGE, true)
        cv.put(COLUMN_IS_READ, requestResolveThread.isRead)
        return cv
    }

    private fun insertOrUpdateMessage(sqlHelper: SQLiteOpenHelper, cv: ContentValues) {
        val sql = (
            "select " + COLUMN_MESSAGE_CORRELATION_ID +
                " from " + TABLE_MESSAGES +
                " where " + COLUMN_MESSAGE_CORRELATION_ID + " = ?"
            )
        val selectionArgs = arrayOf(cv.getAsString(COLUMN_MESSAGE_CORRELATION_ID))
        sqlHelper.readableDatabase.rawQuery(sql, selectionArgs).use { c ->
            if (c.count > 0) {
                sqlHelper.writableDatabase
                    .update(
                        TABLE_MESSAGES,
                        cv,
                        "$COLUMN_MESSAGE_CORRELATION_ID = ? ",
                        arrayOf(cv.getAsString(COLUMN_MESSAGE_CORRELATION_ID))
                    )
            } else {
                sqlHelper.writableDatabase
                    .insert(TABLE_MESSAGES, null, cv)
            }
        }
    }

    private fun insertOrUpdateMessageByTimeStamp(sqlHelper: SQLiteOpenHelper, cv: ContentValues) {
        val sql = (
            "select " + COLUMN_MESSAGE_CORRELATION_ID +
                " from " + TABLE_MESSAGES +
                " where " + COLUMN_MESSAGE_CORRELATION_ID + " = ?"
            )
        val selectionArgs = arrayOf(cv.getAsString(COLUMN_MESSAGE_CORRELATION_ID))
        sqlHelper.readableDatabase.rawQuery(sql, selectionArgs).use { c ->
            if (c.count > 0) {
                sqlHelper.writableDatabase
                    .update(
                        TABLE_MESSAGES,
                        cv,
                        "$COLUMN_TIMESTAMP = ? ",
                        arrayOf(cv.getAsString(COLUMN_MESSAGE_CORRELATION_ID))
                    )
            } else {
                sqlHelper.writableDatabase
                    .insert(TABLE_MESSAGES, null, cv)
            }
        }
    }

    private fun insertOrUpdateSurvey(sqlHelper: SQLiteOpenHelper, survey: Survey) {
        val sql = (
            "select " + COLUMN_SURVEY_SENDING_ID +
                " from " + TABLE_MESSAGES +
                " where " + COLUMN_SURVEY_SENDING_ID + " = ? and " + COLUMN_MESSAGE_TYPE + " = ? "
            )
        val selectionArgs =
            arrayOf(survey.sendingId.toString(), MessageType.SURVEY.ordinal.toString())
        val cv = ContentValues()
        cv.put(COLUMN_MESSAGE_TYPE, MessageType.SURVEY.ordinal)
        cv.put(COLUMN_MESSAGE_CORRELATION_ID, survey.uuid)
        cv.put(COLUMN_SURVEY_SENDING_ID, survey.sendingId)
        cv.put(COLUMN_SURVEY_HIDE_AFTER, survey.hideAfter)
        cv.put(COLUMN_TIMESTAMP, survey.timeStamp)
        cv.put(COLUMN_MESSAGE_SEND_STATE, survey.sentState.value)
        cv.put(COLUMN_DISPLAY_MESSAGE, survey.isDisplayMessage)
        cv.put(COLUMN_IS_READ, survey.isRead)
        if (survey.isRead) {
            cv.put(COLUMN_MESSAGE_SEND_STATE, MessageStatus.READ.value)
        } else {
            cv.put(COLUMN_MESSAGE_SEND_STATE, survey.sentState.value)
        }
        sqlHelper.readableDatabase.rawQuery(sql, selectionArgs).use { c ->
            if (c.count > 0) {
                sqlHelper.writableDatabase
                    .update(
                        TABLE_MESSAGES,
                        cv,
                        "$COLUMN_SURVEY_SENDING_ID = ? ",
                        arrayOf(survey.sendingId.toString())
                    )
            } else {
                sqlHelper.writableDatabase
                    .insert(TABLE_MESSAGES, null, cv)
            }
        }
        questionsTable.putQuestions(sqlHelper, survey.questions ?: arrayListOf(), survey.sendingId)
    }

    private fun setNotSentSurveyDisplayMessageToFalse(
        sqlHelper: SQLiteOpenHelper,
        currentSurveySendingId: Long
    ) {
        val cv = ContentValues()
        cv.put(COLUMN_DISPLAY_MESSAGE, false)
        val whereClause = (
            COLUMN_MESSAGE_TYPE + " = " + MessageType.SURVEY.ordinal +
                " and " + COLUMN_MESSAGE_SEND_STATE + " = " + MessageStatus.FAILED.value +
                " and " + COLUMN_SURVEY_SENDING_ID + " != ?"
            )
        sqlHelper.writableDatabase
            .update(TABLE_MESSAGES, cv, whereClause, arrayOf(currentSurveySendingId.toString()))
    }

    private fun setOldRequestResolveThreadDisplayMessageToFalse(
        sqlHelper: SQLiteOpenHelper,
        uuid: String
    ) {
        val cv = ContentValues()
        cv.put(COLUMN_DISPLAY_MESSAGE, false)
        val whereClause =
            (
                COLUMN_MESSAGE_TYPE + " = " + MessageType.REQUEST_RESOLVE_THREAD.ordinal +
                    " and " + COLUMN_MESSAGE_CORRELATION_ID + " != ?"
                )
        sqlHelper.writableDatabase
            .update(TABLE_MESSAGES, cv, whereClause, arrayOf(uuid))
    }

    private fun isFileDownloaded(fileDescription: FileDescription): Uri? {
        if (fileDescription.incomingName.isNullOrEmpty()) {
            return null
        }
        val outputFile = File(
            getDownloadDir(BaseConfig.getInstance().context),
            generateFileName(fileDescription)
        )
        return if (outputFile.exists()) {
            FileProvider().getUriForFile(
                BaseConfig.getInstance().context,
                outputFile
            )
        } else {
            null
        }
    }

    private fun setProgressAndFileUri(fileDescription: FileDescription, progress: Int, uri: Uri) {
        fileDescription.downloadProgress = progress
        fileDescription.fileUri = uri
    }

    private fun stringToList(text: String?): List<String> {
        return if (text == null) {
            emptyList()
        } else {
            listOf(*text.split(";".toRegex()).toTypedArray())
        }
    }

    private fun listToString(list: List<String>?): String? {
        if (list == null) {
            return null
        }
        val stringBuilder = StringBuilder()
        var divider = ""
        for (item: String in list) {
            stringBuilder.append(divider).append(item)
            divider = ";"
        }
        return stringBuilder.toString()
    }

    private enum class MessageType {
        UNKNOWN, CONSULT_CONNECTED, SYSTEM_MESSAGE, CONSULT_PHRASE, USER_PHRASE, SURVEY, REQUEST_RESOLVE_THREAD
    }
}

private const val TABLE_MESSAGES = "TABLE_MESSAGES"
private const val COLUMN_TABLE_ID = "TABLE_ID"
private const val COLUMN_MESSAGE_CORRELATION_ID = "COLUMN_MESSAGE_CORRELATION_ID"
private const val COLUMN_MESSAGE_ID = "COLUMN_MESSAGE_ID"
private const val COLUMN_TIMESTAMP = "COLUMN_TIMESTAMP"
private const val COLUMN_PHRASE = "COLUMN_PHRASE"
private const val COLUMN_FORMATTED_PHRASE = "COLUMN_FORMATTED_PHRASE"
private const val COLUMN_MESSAGE_TYPE = "COLUMN_MESSAGE_TYPE"
private const val COLUMN_NAME = "COLUMN_NAME"
private const val COLUMN_AVATAR_PATH = "COLUMN_AVATAR_PATH"
private const val COLUMN_MESSAGE_SEND_STATE = "COLUMN_MESSAGE_SEND_STATE"
private const val COLUMN_SEX = "COLUMN_SEX"
private const val COLUMN_CONSULT_ID = "COLUMN_CONSULT_ID"
private const val COLUMN_CONSULT_STATUS = "COLUMN_CONSULT_STATUS"
private const val COLUMN_CONSULT_TITLE = "COLUMN_CONSULT_TITLE"
private const val COLUMN_CONSULT_ORG_UNIT = "COLUMN_CONSULT_ORG_UNIT"
private const val COLUMN_CONSULT_ROLE = "COLUMN_CONSULT_ROLE"
private const val COLUMN_CONNECTION_TYPE = "COLUMN_CONNECTION_TYPE"
private const val COLUMN_IS_READ = "COLUMN_IS_READ"
private const val COLUMN_DISPLAY_MESSAGE = "COLUMN_DISPLAY_MESSAGE"
private const val COLUMN_SURVEY_SENDING_ID = "COLUMN_SURVEY_SENDING_ID"
private const val COLUMN_SURVEY_HIDE_AFTER = "COLUMN_SURVEY_HIDE_AFTER"
private const val COLUMN_THREAD_ID = "COLUMN_THREAD_ID"
private const val COLUMN_BLOCK_INPUT = "COLUMN_BLOCK_INPUT"
private const val COLUMN_SPEECH_STATUS = "COLUMN_SPEECH_STATUS"
