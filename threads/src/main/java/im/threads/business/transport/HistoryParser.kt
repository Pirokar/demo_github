package im.threads.business.transport

import android.text.TextUtils
import com.google.gson.JsonSyntaxException
import im.threads.R
import im.threads.business.config.BaseConfig
import im.threads.business.formatters.ChatItemType
import im.threads.business.formatters.ChatItemType.Companion.fromString
import im.threads.business.formatters.SpeechStatus
import im.threads.business.logger.LoggerEdna.error
import im.threads.business.models.Attachment
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultConnectionMessage
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.FileDescription
import im.threads.business.models.MessageFromHistory
import im.threads.business.models.MessageStatus
import im.threads.business.models.Operator
import im.threads.business.models.QuestionDTO
import im.threads.business.models.Quote
import im.threads.business.models.RequestResolveThread
import im.threads.business.models.SimpleSystemMessage
import im.threads.business.models.Survey
import im.threads.business.models.UserPhrase
import im.threads.business.rest.models.HistoryResponse
import im.threads.business.serviceLocator.core.inject
import im.threads.business.utils.DateHelper
import java.util.ArrayList
import java.util.Date

object HistoryParser {
    private val historyLoader: HistoryLoader by inject()

    fun getChatItems(response: HistoryResponse?): List<ChatItem> {
        var list: List<ChatItem> = ArrayList()
        if (response != null) {
            val responseList = response.messages
            if (responseList != null) {
                list = getChatItems(responseList)
                historyLoader.setupLastItemIdFromHistory(responseList)
            }
        }
        return list
    }

    private fun getChatItems(messages: List<MessageFromHistory?>): List<ChatItem> {
        val out: MutableList<ChatItem> = ArrayList()
        try {
            for (message in messages) {
                if (message == null) {
                    continue
                }
                val uuid = message.uuid
                val timeStamp: Long = message.timeStamp
                val operator = message.operator
                var name: String? = null
                var photoUrl: String? = null
                var operatorId: String? = null
                var sex = false
                var orgUnit: String? = null
                var role: String? = null
                if (operator != null) {
                    name = operator.aliasOrName
                    photoUrl = if (!TextUtils.isEmpty(operator.photoUrl)) operator.photoUrl else null
                    operatorId = if (operator.id != null) operator.id.toString() else null
                    sex = Operator.Gender.MALE == operator.gender
                    orgUnit = operator.orgUnit
                    role = operator.role
                }
                when (fromString(message.type)) {
                    ChatItemType.THREAD_ENQUEUED, ChatItemType.AVERAGE_WAIT_TIME, ChatItemType.PARTING_AFTER_SURVEY,
                    ChatItemType.THREAD_CLOSED, ChatItemType.THREAD_WILL_BE_REASSIGNED, ChatItemType.CLIENT_BLOCKED,
                    ChatItemType.THREAD_IN_PROGRESS -> out.add(
                        getSystemMessageFromHistory(message)
                    )
                    ChatItemType.OPERATOR_JOINED, ChatItemType.OPERATOR_LEFT -> out.add(
                        ConsultConnectionMessage(
                            uuid, operatorId,
                            message.type, name, sex, timeStamp, photoUrl,
                            null, null, orgUnit, role, message.isDisplay,
                            message.text, message.threadId
                        )
                    )
                    ChatItemType.SURVEY -> {
                        val survey = getSurveyFromJsonString(message.text)
                        if (survey != null) {
                            survey.isRead = message.isRead
                            survey.phraseTimeStamp = message.timeStamp
                            for (questionDTO in survey.questions) {
                                questionDTO.phraseTimeStamp = message.timeStamp
                            }
                            out.add(survey)
                        }
                    }
                    ChatItemType.SURVEY_QUESTION_ANSWER -> out.add(getCompletedSurveyFromHistory(message))
                    ChatItemType.REQUEST_CLOSE_THREAD -> out.add(
                        RequestResolveThread(
                            uuid,
                            message.hideAfter,
                            timeStamp,
                            message.threadId,
                            message.isRead
                        )
                    )
                    else -> {
                        var phraseText: String? = ""
                        if (message.text != null) {
                            phraseText = message.text
                        } else if (message.speechText != null) {
                            phraseText = message.speechText
                        }
                        val fileDescription = if (message.attachments != null) fileDescriptionFromList(message.attachments) else null
                        if (fileDescription != null) {
                            fileDescription.from = name
                            fileDescription.timeStamp = timeStamp
                        }
                        val quote = if (message.quotes != null) quoteFromList(message.quotes) else null
                        quote?.fileDescription?.timeStamp = timeStamp
                        if (message.operator != null) {
                            out.add(
                                ConsultPhrase(
                                    uuid,
                                    fileDescription,
                                    quote,
                                    name,
                                    phraseText,
                                    message.formattedText,
                                    timeStamp,
                                    operatorId,
                                    photoUrl,
                                    message.isRead,
                                    message.operator.status,
                                    false,
                                    message.threadId,
                                    message.quickReplies,
                                    if (message.settings != null) message.settings!!.isBlockInput else null,
                                    SpeechStatus.fromString(message.speechStatus)
                                )
                            )
                        } else {
                            if (fileDescription != null) {
                                fileDescription.from = BaseConfig.instance.context.getString(R.string.ecc_I)
                            }
                            val sentState = if (message.isRead) MessageStatus.READ else MessageStatus.SENT
                            val userPhrase =
                                UserPhrase(uuid, phraseText, quote, timeStamp, fileDescription, sentState, message.threadId)
                            userPhrase.isRead = message.isRead
                            out.add(userPhrase)
                        }
                    }
                }
            }
            out.sortWith { ci1: ChatItem, ci2: ChatItem -> ci1.timeStamp.compareTo(ci2.timeStamp) }
        } catch (e: Exception) {
            error("error while formatting: $messages", e)
        }
        return out
    }

    private fun getSurveyFromJsonString(text: String): Survey? {
        return try {
            val survey = BaseConfig.instance.gson.fromJson(text, Survey::class.java)
            val time = Date().time
            survey.phraseTimeStamp = time
            survey.sentState = MessageStatus.FAILED
            survey.isDisplayMessage = true
            for (questionDTO in survey.questions) {
                questionDTO.phraseTimeStamp = time
            }
            survey
        } catch (e: JsonSyntaxException) {
            error("getSurveyFromJsonString", e)
            null
        }
    }

    private fun getCompletedSurveyFromHistory(message: MessageFromHistory): Survey {
        val survey = Survey(
            message.uuid,
            message.sendingId,
            message.timeStamp,
            MessageStatus.READ,
            message.isRead,
            message.isDisplay
        )
        val question = QuestionDTO()
        question.id = message.questionId
        question.phraseTimeStamp = message.timeStamp
        question.text = message.text
        question.rate = message.rate
        question.scale = message.scale
        question.sendingId = message.sendingId
        question.simple = message.isSimple
        survey.questions = listOf(question)
        return survey
    }

    private fun getSystemMessageFromHistory(message: MessageFromHistory): SimpleSystemMessage {
        return SimpleSystemMessage(message.uuid, message.type, message.timeStamp, message.text, message.threadId)
    }

    private fun quoteFromList(quotes: List<MessageFromHistory?>): Quote? {
        var quote: Quote? = null
        if (quotes.isNotEmpty() && quotes[0] != null) {
            val quoteFromHistory = quotes[0]
            var quoteFileDescription: FileDescription? = null
            var quoteString: String? = null
            val receivedDateString = quoteFromHistory!!.receivedDate
            val timestamp =
                if (receivedDateString == null || receivedDateString.isEmpty()) {
                    System.currentTimeMillis()
                } else {
                    DateHelper.getMessageTimestampFromDateString(
                        receivedDateString
                    )
                }
            if (quoteFromHistory.text != null) {
                quoteString = quoteFromHistory.text
            }
            if (quoteFromHistory.attachments != null && quoteFromHistory.attachments.size > 0 &&
                quoteFromHistory.attachments[0].result != null
            ) {
                quoteFileDescription = fileDescriptionFromList(quoteFromHistory.attachments)
            }
            val authorName: String? = if (quoteFromHistory.operator != null) {
                quoteFromHistory.operator.aliasOrName
            } else {
                BaseConfig.instance.context.getString(R.string.ecc_I)
            }
            if (quoteString != null || quoteFileDescription != null) {
                quote = Quote(quoteFromHistory.uuid, authorName, quoteString, quoteFileDescription, timestamp)
            }
            if (quoteFileDescription != null) {
                quoteFileDescription.from = authorName
            }
        }
        return quote
    }

    private fun fileDescriptionFromList(attachments: List<Attachment?>): FileDescription? {
        var fileDescription: FileDescription? = null
        if (attachments.isNotEmpty()) {
            val attachment = attachments[0]
            if (attachment != null) {
                val incomingName = attachment.name
                val mimeType = attachment.type
                var size: Long = 0
                val metaData = attachment.optional
                if (metaData != null) {
                    size = metaData.size
                }
                fileDescription = FileDescription(
                    null,
                    null,
                    size,
                    0
                )
                fileDescription.downloadPath = attachment.result
                fileDescription.incomingName = incomingName
                fileDescription.mimeType = mimeType
                fileDescription.state = attachment.state
                if (attachment.errorCode != null) {
                    fileDescription.errorCode = attachment.getErrorCodeState()
                    fileDescription.errorMessage = attachment.errorMessage
                }
            }
        }
        return fileDescription
    }
}
