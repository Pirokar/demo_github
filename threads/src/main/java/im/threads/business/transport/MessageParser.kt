package im.threads.business.transport

import com.google.gson.JsonObject
import im.threads.business.config.BaseConfig
import im.threads.business.formatters.ChatItemType
import im.threads.business.formatters.ChatItemType.Companion.fromString
import im.threads.business.formatters.SpeechStatus.Companion.fromString
import im.threads.business.logger.LoggerEdna.error
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultConnectionMessage
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.ConsultRole
import im.threads.business.models.FileDescription
import im.threads.business.models.MessageRead
import im.threads.business.models.MessageStatus
import im.threads.business.models.Quote
import im.threads.business.models.RequestResolveThread
import im.threads.business.models.SearchingConsult
import im.threads.business.models.SimpleSystemMessage
import im.threads.business.models.SpeechMessageUpdate
import im.threads.business.models.Survey
import im.threads.business.models.UserPhrase
import im.threads.business.models.enums.ModificationStateEnum
import im.threads.business.transport.models.Attachment
import im.threads.business.transport.models.MessageContent
import im.threads.business.transport.models.OperatorJoinedContent
import im.threads.business.transport.models.RequestResolveThreadContent
import im.threads.business.transport.models.SpeechMessageUpdatedContent
import im.threads.business.transport.models.SurveyContent
import im.threads.business.transport.models.SystemMessageContent

class MessageParser {
    /**
     * @return null, если не удалось распознать формат сообщения.
     */
    fun format(
        sentAt: Long,
        shortMessage: String?,
        fullMessage: JsonObject?
    ): ChatItem? {
        return if (fullMessage != null) {
            return when (fromString(getType(fullMessage))) {
                ChatItemType.CLIENT_BLOCKED,
                ChatItemType.THREAD_ENQUEUED,
                ChatItemType.AVERAGE_WAIT_TIME,
                ChatItemType.PARTING_AFTER_SURVEY,
                ChatItemType.THREAD_CLOSED,
                ChatItemType.THREAD_WILL_BE_REASSIGNED,
                ChatItemType.CLIENT_PERSONAL_DATA_PROCESSING,
                ChatItemType.THREAD_IN_PROGRESS -> getSystemMessage(
                    sentAt,
                    fullMessage
                )

                ChatItemType.OPERATOR_JOINED,
                ChatItemType.OPERATOR_LEFT -> getConsultConnection(
                    sentAt,
                    shortMessage,
                    fullMessage
                )

                ChatItemType.SURVEY -> getSurvey(sentAt, fullMessage)
                ChatItemType.REQUEST_CLOSE_THREAD -> getRequestResolveThread(
                    sentAt,
                    fullMessage
                )

                ChatItemType.OPERATOR_LOOKUP_STARTED -> SearchingConsult()
                ChatItemType.NONE,
                ChatItemType.MESSAGES_READ -> getMessageRead(fullMessage)
                ChatItemType.SCENARIO -> null
                ChatItemType.ATTACHMENT_SETTINGS -> null
                ChatItemType.SPEECH_MESSAGE_UPDATED -> {
                    val (uuid, speechStatus, attachments) = BaseConfig.getInstance().gson.fromJson(
                        fullMessage,
                        SpeechMessageUpdatedContent::class.java
                    )
                    if (uuid != null && attachments != null) {
                        return SpeechMessageUpdate(
                            uuid,
                            fromString(speechStatus),
                            getFileDescription(attachments, null, sentAt)!!
                        )
                    } else {
                        error("SPEECH_MESSAGE_UPDATED with invalid params")
                    }
                    null
                }

                ChatItemType.MESSAGE,
                ChatItemType.MESSAGE_EDITED,
                ChatItemType.MESSAGE_DELETED,
                ChatItemType.ON_HOLD -> getPhrase(
                    sentAt,
                    shortMessage,
                    fullMessage
                )

                else -> getPhrase(sentAt, shortMessage, fullMessage)
            }
        } else {
            null
        }
    }

    fun getType(fullMessage: JsonObject): String {
        return fullMessage[MessageAttributes.TYPE].asString
    }

    private fun getMessageRead(fullMessage: JsonObject): MessageRead {
        return MessageRead(
            listOf(
                *fullMessage[MessageAttributes.READ_MESSAGE_ID].asString.split(",".toRegex())
                    .dropLastWhile { it.isEmpty() }.toTypedArray()
            )
        )
    }

    private fun getConsultConnection(
        sentAt: Long,
        shortMessage: String?,
        fullMessage: JsonObject
    ): ConsultConnectionMessage {
        val content =
            BaseConfig.getInstance().gson.fromJson(fullMessage, OperatorJoinedContent::class.java)
        val operator = content.operator
        return ConsultConnectionMessage(
            content.uuid, operator!!.id.toString(),
            content.type,
            operator.aliasOrName,
            "male".equals(operator.gender, ignoreCase = true),
            sentAt,
            operator.photoUrl,
            operator.status,
            shortMessage?.split(" ".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
                ?.get(0),
            operator.organizationUnit,
            operator.role,
            content.display,
            content.text,
            content.threadId
        )
    }

    private fun getSystemMessage(sentAt: Long, fullMessage: JsonObject): SimpleSystemMessage {
        val content =
            BaseConfig.getInstance().gson.fromJson(fullMessage, SystemMessageContent::class.java)
        return SimpleSystemMessage(
            content.uuid,
            content.type,
            sentAt,
            content.text,
            content.threadId
        )
    }

    private fun getSurvey(sentAt: Long, fullMessage: JsonObject): Survey {
        val content = BaseConfig.getInstance().gson.fromJson(fullMessage, SurveyContent::class.java)
        val survey = BaseConfig.getInstance().gson.fromJson(content.content.toString(), Survey::class.java)
        survey.uuid = content.uuid
        survey.timeStamp = sentAt
        survey.sentState = MessageStatus.FAILED
        survey.isDisplayMessage = true
        survey.isRead = false
        val questions = survey.questions ?: arrayListOf()
        for (questionDTO in questions) {
            questionDTO.phraseTimeStamp = sentAt
        }
        return survey
    }

    private fun getRequestResolveThread(
        sentAt: Long,
        fullMessage: JsonObject
    ): RequestResolveThread {
        val content =
            BaseConfig.getInstance().gson.fromJson(fullMessage, RequestResolveThreadContent::class.java)
        return RequestResolveThread(
            content.uuid,
            content.hideAfter,
            sentAt,
            content.threadId,
            false
        )
    }

    private fun getPhrase(sentAt: Long, shortMessage: String?, fullMessage: JsonObject): ChatItem? {
        val (
            uuid,
            text,
            speechText,
            formattedText,
            _, threadId,
            operator,
            attachments,
            quotes,
            quickReplies,
            settings,
            speechStatus,
            read,
            modified,
            messageUuid
        ) = BaseConfig.getInstance().gson.fromJson(
            fullMessage,
            MessageContent::class.java
        )

        if (text == null && attachments == null && quotes == null &&
            ModificationStateEnum.fromString(modified) != ModificationStateEnum.DELETED
        ) {
            return null
        }
        var isMessageRead: Boolean? = false
        if (read != null) {
            isMessageRead = read
        }
        val phrase: String? = text ?: (speechText ?: shortMessage)
        var quote: Quote? = null
        if (quotes != null) {
            quote = getQuote(quotes)
        }
        return if (operator != null ||
            ModificationStateEnum.fromString(modified) != ModificationStateEnum.NONE
        ) {
            val operatorId = operator?.id.toString()
            val name = operator?.aliasOrName
            val photoUrl = operator?.photoUrl
            val status = operator?.status
            val gender =
                operator?.gender == null || "male".equals(operator.gender, ignoreCase = true)
            var fileDescription: FileDescription? = null
            if (attachments != null) {
                fileDescription = getFileDescription(attachments, name, sentAt)
            }
            ConsultPhrase(
                messageUuid ?: uuid,
                fileDescription,
                ModificationStateEnum.fromString(modified),
                quote,
                name,
                phrase,
                formattedText,
                sentAt,
                operatorId,
                photoUrl,
                false,
                status,
                gender,
                threadId,
                quickReplies,
                settings?.isBlockInput,
                fromString(speechStatus),
                ConsultRole.consultRoleFromString(operator?.role)
            )
        } else {
            var fileDescription: FileDescription? = null
            if (attachments != null) {
                fileDescription = getFileDescription(attachments, null, sentAt)
            }
            val userPhrase = UserPhrase(
                messageUuid ?: uuid,
                phrase,
                quote,
                sentAt,
                fileDescription,
                threadId
            )
            userPhrase.sentState = MessageStatus.SENT
            userPhrase.isRead = isMessageRead!!
            userPhrase
        }
    }

    private fun getFileDescription(
        attachments: List<Attachment?>?,
        from: String?,
        timeStamp: Long
    ): FileDescription? {
        var fileDescription: FileDescription? = null
        if (!attachments.isNullOrEmpty() && attachments[0] != null) {
            val attachment = attachments[0]
            fileDescription = FileDescription(
                from,
                null,
                attachment!!.size,
                timeStamp
            )
            fileDescription.downloadPath = attachment.result
            fileDescription.originalPath = attachment.originalUrl
            fileDescription.incomingName = attachment.name
            fileDescription.mimeType = attachment.type
            fileDescription.state = attachment.state
            if (attachment.errorCode != null) {
                fileDescription.errorCode = attachment.getErrorCodeState()
                fileDescription.errorMessage = attachment.errorMessage
            }
        }
        return fileDescription
    }

    private fun getQuote(quotes: List<im.threads.business.transport.models.Quote?>?): Quote? {
        if (!quotes.isNullOrEmpty() && quotes[0] != null) {
            val quote = quotes[0]
            val authorName = quote?.operator?.aliasOrName
            val timestamp = quote?.receivedDate?.time ?: System.currentTimeMillis()
            var fileDescription: FileDescription? = null
            if (quote?.attachments != null) {
                fileDescription = getFileDescription(quote.attachments, authorName, timestamp)
            }
            if (quote?.uuid != null && (quote.text != null || fileDescription != null)) {
                return Quote(quote.uuid, authorName, quote.text, fileDescription, timestamp)
            }
        }
        return null
    }
}
