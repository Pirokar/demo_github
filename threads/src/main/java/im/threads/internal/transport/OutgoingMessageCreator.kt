package im.threads.internal.transport

import android.content.Context
import android.net.Uri
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import im.threads.internal.formatters.ChatItemType
import im.threads.internal.model.CAMPAIGN_DATE_FORMAT
import im.threads.internal.model.ConsultInfo
import im.threads.internal.model.FileDescription
import im.threads.internal.model.Survey
import im.threads.internal.model.UserPhrase
import im.threads.internal.retrofit.OldThreadsBackendApi.Companion.API_VERSION
import im.threads.internal.utils.AppInfoHelper
import im.threads.internal.utils.DeviceInfoHelper
import im.threads.internal.utils.FileUtils.getFileName
import im.threads.internal.utils.FileUtils.getFileSize
import im.threads.internal.utils.FileUtils.getMimeType
import im.threads.internal.utils.PrefUtils.Companion.appMarker
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object OutgoingMessageCreator {
    fun createInitChatMessage(clientId: String?, data: String?): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.apply {
            addProperty(MessageAttributes.CLIENT_ID, clientId)
            addProperty(MessageAttributes.TYPE, ChatItemType.INIT_CHAT.name)
            addProperty(MessageAttributes.DATA, data)
            addProperty(MessageAttributes.APP_MARKER_KEY, appMarker)
        }
        return jsonObject
    }

    fun createEnvironmentMessage(
        clientName: String?,
        clientId: String?,
        clientIdEncrypted: Boolean,
        data: String?,
        ctx: Context?
    ): JsonObject {
        val jsonObject = JsonObject().apply {
            addProperty("name", clientName)
            addProperty(MessageAttributes.CLIENT_ID, clientId)
            addProperty(MessageAttributes.CLIENT_ID_ENCRYPTED, clientIdEncrypted)
            if (!data.isNullOrEmpty()) addProperty(MessageAttributes.DATA, data)
            addProperty("platform", "Android")
            addProperty("osVersion", DeviceInfoHelper.getOsVersion())
            addProperty("device", DeviceInfoHelper.getDeviceName())
            addProperty("ip", DeviceInfoHelper.getIpAddress())
            addProperty("appVersion", AppInfoHelper.getAppVersion())
            addProperty("appName", AppInfoHelper.getAppName())
            addProperty(MessageAttributes.APP_BUNDLE_KEY, AppInfoHelper.getAppId())
            addProperty(MessageAttributes.APP_MARKER_KEY, appMarker)
            addProperty("libVersion", AppInfoHelper.getLibVersion())
            addProperty("clientLocale", DeviceInfoHelper.getLocale(ctx))
            addProperty("chatApiVersion", API_VERSION)
            addProperty(MessageAttributes.TYPE, ChatItemType.CLIENT_INFO.name)
        }
        return jsonObject
    }

    fun createMessageTyping(clientId: String?, input: String?): JsonObject {
        val jsonObject = JsonObject().apply {
            addProperty(MessageAttributes.CLIENT_ID, clientId)
            addProperty(MessageAttributes.TYPE, ChatItemType.TYPING.name)
            addProperty(MessageAttributes.TYPING_DRAFT, input)
            addProperty(MessageAttributes.APP_MARKER_KEY, appMarker)
        }
        return jsonObject
    }

    fun createRatingDoneMessage(survey: Survey, clientId: String?, appMarker: String?): JsonObject {
        val jsonObject = JsonObject().apply {
            addProperty(MessageAttributes.CLIENT_ID, clientId)
            addProperty(MessageAttributes.TYPE, ChatItemType.SURVEY_QUESTION_ANSWER.name)
            addProperty("sendingId", survey.sendingId)
            addProperty("questionId", survey.questions[0].id)
            addProperty("rate", survey.questions[0].rate)
            addProperty("text", survey.questions[0].text)
            addProperty(MessageAttributes.APP_MARKER_KEY, appMarker)
        }
        return jsonObject
    }

    fun createResolveThreadMessage(clientId: String?): JsonObject {
        val jsonObject = JsonObject().apply {
            addProperty(MessageAttributes.CLIENT_ID, clientId)
            addProperty(MessageAttributes.TYPE, ChatItemType.CLOSE_THREAD.name)
            addProperty(MessageAttributes.APP_MARKER_KEY, appMarker)
        }
        return jsonObject
    }

    fun createReopenThreadMessage(clientId: String?): JsonObject {
        val jsonObject = JsonObject().apply {
            addProperty(MessageAttributes.CLIENT_ID, clientId)
            addProperty(MessageAttributes.TYPE, ChatItemType.REOPEN_THREAD.name)
            addProperty(MessageAttributes.APP_MARKER_KEY, appMarker)
        }
        return jsonObject
    }

    fun createRatingReceivedMessage(sendingId: Long, clientId: String?): JsonObject {
        val jsonObject = JsonObject().apply {
            addProperty(MessageAttributes.CLIENT_ID, clientId)
            addProperty(MessageAttributes.TYPE, ChatItemType.SURVEY_PASSED.name)
            addProperty(MessageAttributes.APP_MARKER_KEY, appMarker)
            addProperty("sendingId", sendingId)
        }
        return jsonObject
    }

    fun createMessageClientOffline(clientId: String?): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty(MessageAttributes.CLIENT_ID, clientId)
        jsonObject.addProperty(MessageAttributes.TYPE, ChatItemType.CLIENT_OFFLINE.name)
        jsonObject.addProperty(MessageAttributes.APP_MARKER_KEY, appMarker)
        return jsonObject
    }

    fun createMessageUpdateLocation(
        latitude: Double,
        longitude: Double,
        clientName: String?,
        clientId: String?,
        clientIdEncrypted: Boolean,
        ctx: Context?
    ): JsonObject {
        val jsonObject = JsonObject().apply {
            val location = JsonObject().apply {
                addProperty(MessageAttributes.COORDINATES, "$latitude, $longitude")
            }
            add(MessageAttributes.DATA, location)
            addProperty("name", clientName)
            addProperty(MessageAttributes.CLIENT_ID, clientId)
            addProperty(MessageAttributes.CLIENT_ID_ENCRYPTED, clientIdEncrypted)
            addProperty("platform", "Android")
            addProperty("osVersion", DeviceInfoHelper.getOsVersion())
            addProperty("device", DeviceInfoHelper.getDeviceName())
            addProperty("ip", DeviceInfoHelper.getIpAddress())
            addProperty("appVersion", AppInfoHelper.getAppVersion())
            addProperty("appName", AppInfoHelper.getAppName())
            addProperty(MessageAttributes.APP_BUNDLE_KEY, AppInfoHelper.getAppId())
            addProperty(MessageAttributes.APP_MARKER_KEY, appMarker)
            addProperty("libVersion", AppInfoHelper.getLibVersion())
            addProperty("clientLocale", DeviceInfoHelper.getLocale(ctx))
            addProperty("chatApiVersion", API_VERSION)
            addProperty(MessageAttributes.TYPE, ChatItemType.UPDATE_LOCATION.name)
        }
        return jsonObject
    }

    fun createUserPhraseMessage(
        userPhrase: UserPhrase,
        consultInfo: ConsultInfo?,
        quoteMfmsFilePath: String?,
        mfmsFilePath: String?,
        clientId: String?
    ): JsonObject {
        val phrase = userPhrase.phraseText
        val quote = userPhrase.quote
        val fileDescription = userPhrase.fileDescription
        val campaignMessage = userPhrase.campaignMessage
        val formattedMessage = JsonObject().apply {
            addProperty(MessageAttributes.UUID, userPhrase.id)
            addProperty(MessageAttributes.CLIENT_ID, clientId)
            addProperty(MessageAttributes.TEXT, phrase ?: "")
            addProperty(MessageAttributes.APP_MARKER_KEY, appMarker)
        }
        val quotes = JsonArray().apply {
            campaignMessage?.let {
                val sdf = SimpleDateFormat(CAMPAIGN_DATE_FORMAT, Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val quoteJson = JsonObject().apply {
                    addProperty(MessageAttributes.UUID, it.chatMessageId)
                    addProperty(MessageAttributes.TEXT, it.text)
                    addProperty(MessageAttributes.IS_MASS_PUSH_MESSAGE, true)
                    addProperty(MessageAttributes.CAMPAIGN, it.campaign)
                    addProperty(
                        MessageAttributes.RECEIVED_DATE,
                        sdf.format(it.receivedDate)
                    )
                }
                add(quoteJson)

                val routingParams = JsonObject().apply {
                    addProperty(MessageAttributes.PRIORITY, it.priority)
                    addProperty(MessageAttributes.SKILL_ID, it.skillId)
                    addProperty(MessageAttributes.EXPIRED_AT, sdf.format(it.expiredAt))
                }
                formattedMessage.add(MessageAttributes.ROUTING_PARAMS, routingParams)
            }
        }

        quote?.let {
            val quoteJson = JsonObject().apply {
                if (!it.text.isNullOrEmpty()) {
                    addProperty(MessageAttributes.TEXT, it.text)
                }
                consultInfo?.let {
                    add("operator", it.toJson())
                }
                it.fileDescription?.let {
                    if (!quoteMfmsFilePath.isNullOrEmpty()) {
                        add(
                            MessageAttributes.ATTACHMENTS,
                            attachmentsFromFileDescription(it, quoteMfmsFilePath)
                        )
                    }
                }
                if (!userPhrase.quote.uuid.isNullOrEmpty()) {
                    addProperty(MessageAttributes.UUID, userPhrase.quote.uuid)
                }
            }
            quotes.add(quoteJson)
        }
        formattedMessage.add(MessageAttributes.QUOTES, quotes)

        fileDescription?.let {
            if (!mfmsFilePath.isNullOrEmpty()) {
                formattedMessage.add(
                    MessageAttributes.ATTACHMENTS,
                    attachmentsFromFileDescription(fileDescription, mfmsFilePath)
                )
            }
        }
        return formattedMessage
    }

    private fun attachmentsFromFileDescription(
        fileDescription: FileDescription?,
        isSelfie: Boolean
    ): JsonArray {
        val attachments = JsonArray()
        fileDescription?.let {
            val optional = JsonObject().apply {
                addProperty(MessageAttributes.TYPE, getMimeType(it))
                val fileUri = fileDescription.fileUri
                fileUri?.let {
                    addProperty("name", getFileName(it))
                    addProperty("size", getFileSize(it))
                }
                addProperty("lastModified", 0)
            }
            val attachment = JsonObject().apply {
                addProperty("isSelfie", isSelfie)
                add("optional", optional)
            }
            attachments.add(attachment)
        }
        return attachments
    }

    private fun attachmentsFromMfmsPath(fileDescription: FileDescription?): JsonArray {
        val attachments = JsonArray()
        fileDescription?.let {
            val optional = JsonObject().apply {
                if (!it.incomingName.isNullOrEmpty()) {
                    addProperty(MessageAttributes.TYPE, getMimeType(Uri.parse(it.incomingName)))
                }
                addProperty("name", it.incomingName)
                addProperty("size", it.size)
                addProperty("lastModified", System.currentTimeMillis())
            }
            val attachment = JsonObject().apply {
                addProperty("isSelfie", it.isSelfie)
                add("optional", optional)
            }
            attachments.add(attachment)
        }
        return attachments
    }

    private fun attachmentsFromFileDescription(
        fileDescription: FileDescription?,
        mfmsFilepath: String
    ): JsonArray? {
        var attachments: JsonArray? = null

        fileDescription?.let {
            val file = it
            it.fileUri?.let {
                attachments =
                    attachmentsFromFileDescription(file, file.isSelfie)
            }
            it.downloadPath?.let {
                attachments = attachmentsFromMfmsPath(file)
            }
        }
        attachments?.let {
            (it[0] as JsonObject).addProperty("result", mfmsFilepath)
        }
        return attachments
    }
}
