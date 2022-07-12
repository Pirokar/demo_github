package im.threads.internal.transport

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import im.threads.internal.formatters.ChatItemType
import im.threads.internal.model.CAMPAIGN_DATE_FORMAT
import im.threads.internal.model.ConsultInfo
import im.threads.internal.model.FileDescription
import im.threads.internal.model.Survey
import im.threads.internal.model.UserPhrase
import im.threads.internal.retrofit.OldThreadsApi
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
        jsonObject.addProperty(MessageAttributes.CLIENT_ID, clientId)
        jsonObject.addProperty(MessageAttributes.TYPE, ChatItemType.INIT_CHAT.name)
        jsonObject.addProperty(MessageAttributes.DATA, data)
        jsonObject.addProperty(MessageAttributes.APP_MARKER_KEY, appMarker)
        return jsonObject
    }

    fun createEnvironmentMessage(
        clientName: String?,
        clientId: String?,
        clientIdEncrypted: Boolean,
        data: String?,
        ctx: Context?
    ): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty("name", clientName)
        jsonObject.addProperty(MessageAttributes.CLIENT_ID, clientId)
        jsonObject.addProperty(MessageAttributes.CLIENT_ID_ENCRYPTED, clientIdEncrypted)
        if (!TextUtils.isEmpty(data)) {
            jsonObject.addProperty(MessageAttributes.DATA, data)
        }
        jsonObject.addProperty("platform", "Android")
        jsonObject.addProperty("osVersion", DeviceInfoHelper.getOsVersion())
        jsonObject.addProperty("device", DeviceInfoHelper.getDeviceName())
        jsonObject.addProperty("ip", DeviceInfoHelper.getIpAddress())
        jsonObject.addProperty("appVersion", AppInfoHelper.getAppVersion())
        jsonObject.addProperty("appName", AppInfoHelper.getAppName())
        jsonObject.addProperty(MessageAttributes.APP_BUNDLE_KEY, AppInfoHelper.getAppId())
        jsonObject.addProperty(MessageAttributes.APP_MARKER_KEY, appMarker)
        jsonObject.addProperty("libVersion", AppInfoHelper.getLibVersion())
        jsonObject.addProperty("clientLocale", DeviceInfoHelper.getLocale(ctx))
        jsonObject.addProperty("chatApiVersion", OldThreadsApi.API_VERSION)
        jsonObject.addProperty(MessageAttributes.TYPE, ChatItemType.CLIENT_INFO.name)
        return jsonObject
    }

    fun createMessageTyping(clientId: String?, input: String?): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty(MessageAttributes.CLIENT_ID, clientId)
        jsonObject.addProperty(MessageAttributes.TYPE, ChatItemType.TYPING.name)
        jsonObject.addProperty(MessageAttributes.TYPING_DRAFT, input)
        jsonObject.addProperty(MessageAttributes.APP_MARKER_KEY, appMarker)
        return jsonObject
    }

    fun createRatingDoneMessage(survey: Survey, clientId: String?, appMarker: String?): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty(MessageAttributes.CLIENT_ID, clientId)
        jsonObject.addProperty(MessageAttributes.TYPE, ChatItemType.SURVEY_QUESTION_ANSWER.name)
        jsonObject.addProperty("sendingId", survey.sendingId)
        jsonObject.addProperty("questionId", survey.questions[0].id)
        jsonObject.addProperty("rate", survey.questions[0].rate)
        jsonObject.addProperty("text", survey.questions[0].text)
        jsonObject.addProperty(MessageAttributes.APP_MARKER_KEY, appMarker)
        return jsonObject
    }

    fun createResolveThreadMessage(clientId: String?): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty(MessageAttributes.CLIENT_ID, clientId)
        jsonObject.addProperty(MessageAttributes.TYPE, ChatItemType.CLOSE_THREAD.name)
        jsonObject.addProperty(MessageAttributes.APP_MARKER_KEY, appMarker)
        return jsonObject
    }

    fun createReopenThreadMessage(clientId: String?): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty(MessageAttributes.CLIENT_ID, clientId)
        jsonObject.addProperty(MessageAttributes.TYPE, ChatItemType.REOPEN_THREAD.name)
        jsonObject.addProperty(MessageAttributes.APP_MARKER_KEY, appMarker)
        return jsonObject
    }

    fun createRatingReceivedMessage(sendingId: Long, clientId: String?): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty(MessageAttributes.CLIENT_ID, clientId)
        jsonObject.addProperty(MessageAttributes.TYPE, ChatItemType.SURVEY_PASSED.name)
        jsonObject.addProperty(MessageAttributes.APP_MARKER_KEY, appMarker)
        jsonObject.addProperty("sendingId", sendingId)
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
        latitude: Long,
        longitude: Long,
        clientName: String?,
        clientId: String?,
        clientIdEncrypted: Boolean,
        ctx: Context?
    ): JsonObject {
        val jsonObject = JsonObject()
        val location = JsonObject()
        location.addProperty(MessageAttributes.COORDINATES, "$latitude, $longitude")
        jsonObject.add(MessageAttributes.DATA, location)
        jsonObject.addProperty("name", clientName)
        jsonObject.addProperty(MessageAttributes.CLIENT_ID, clientId)
        jsonObject.addProperty(MessageAttributes.CLIENT_ID_ENCRYPTED, clientIdEncrypted)
        jsonObject.addProperty("platform", "Android")
        jsonObject.addProperty("osVersion", DeviceInfoHelper.getOsVersion())
        jsonObject.addProperty("device", DeviceInfoHelper.getDeviceName())
        jsonObject.addProperty("ip", DeviceInfoHelper.getIpAddress())
        jsonObject.addProperty("appVersion", AppInfoHelper.getAppVersion())
        jsonObject.addProperty("appName", AppInfoHelper.getAppName())
        jsonObject.addProperty(MessageAttributes.APP_BUNDLE_KEY, AppInfoHelper.getAppId())
        jsonObject.addProperty(MessageAttributes.APP_MARKER_KEY, appMarker)
        jsonObject.addProperty("libVersion", AppInfoHelper.getLibVersion())
        jsonObject.addProperty("clientLocale", DeviceInfoHelper.getLocale(ctx))
        jsonObject.addProperty("chatApiVersion", OldThreadsApi.API_VERSION)
        jsonObject.addProperty(MessageAttributes.TYPE, ChatItemType.UPDATE_LOCATION.name)
        return jsonObject
    }

    fun createUserPhraseMessage(
        userPhrase: UserPhrase,
        consultInfo: ConsultInfo?,
        quoteMfmsFilePath: String?,
        mfmsFilePath: String?,
        clientId: String?
    ): JsonObject {
        val quote = userPhrase.quote
        val fileDescription = userPhrase.fileDescription
        val campaignMessage = userPhrase.campaignMessage
        val formattedMessage = JsonObject()
        formattedMessage.addProperty(MessageAttributes.UUID, userPhrase.id)
        formattedMessage.addProperty(MessageAttributes.CLIENT_ID, clientId)
        val phrase = userPhrase.phraseText
        formattedMessage.addProperty(MessageAttributes.TEXT, phrase ?: "")
        formattedMessage.addProperty(MessageAttributes.APP_MARKER_KEY, appMarker)
        val quotes = JsonArray()
        if (campaignMessage != null) {
            val sdf = SimpleDateFormat(CAMPAIGN_DATE_FORMAT, Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val quoteJson = JsonObject()
            quoteJson.addProperty(MessageAttributes.UUID, campaignMessage.chatMessageId)
            quoteJson.addProperty(MessageAttributes.TEXT, campaignMessage.text)
            quoteJson.addProperty(MessageAttributes.IS_MASS_PUSH_MESSAGE, true)
            quoteJson.addProperty(MessageAttributes.CAMPAIGN, campaignMessage.campaign)
            quoteJson.addProperty(
                MessageAttributes.RECEIVED_DATE,
                sdf.format(campaignMessage.receivedDate)
            )
            quotes.add(quoteJson)
            val routingParams = JsonObject()
            routingParams.addProperty(MessageAttributes.PRIORITY, campaignMessage.priority)
            routingParams.addProperty(MessageAttributes.SKILL_ID, campaignMessage.skillId)
            routingParams.addProperty(
                MessageAttributes.EXPIRED_AT,
                sdf.format(campaignMessage.expiredAt)
            )
            formattedMessage.add(MessageAttributes.ROUTING_PARAMS, routingParams)
        } else if (quote != null) {
            val quoteJson = JsonObject()
            quotes.add(quoteJson)
            if (!TextUtils.isEmpty(quote.text)) {
                quoteJson.addProperty(MessageAttributes.TEXT, quote.text)
            }
            if (consultInfo != null) {
                quoteJson.add("operator", consultInfo.toJson()) // TODO #THREADS-5270 What is it for?
            }
            if (quote.fileDescription != null && quoteMfmsFilePath != null) {
                quoteJson.add(
                    MessageAttributes.ATTACHMENTS,
                    attachmentsFromFileDescription(quote.fileDescription, quoteMfmsFilePath)
                )
            }
            if (!userPhrase.quote.uuid.isNullOrEmpty()) {
                quoteJson.addProperty(MessageAttributes.UUID, userPhrase.quote.uuid)
            }
        }
        if (quotes.size() > 0) {
            formattedMessage.add(MessageAttributes.QUOTES, quotes)
        }
        formattedMessage.add(MessageAttributes.QUOTES, quotes)
        if (fileDescription != null && mfmsFilePath != null) {
            formattedMessage.add(
                MessageAttributes.ATTACHMENTS,
                attachmentsFromFileDescription(fileDescription, mfmsFilePath)
            )
        }
        return formattedMessage
    }

    private fun attachmentsFromFileDescription(fd: FileDescription?, isSelfie: Boolean): JsonArray {
        val attachments = JsonArray()
        val attachment = JsonObject()
        attachments.add(attachment)
        val optional = JsonObject()
        attachment.addProperty("isSelfie", isSelfie)
        attachment.add("optional", optional)
        optional.addProperty(
            MessageAttributes.TYPE,
            getMimeType(
                fd!!
            )
        )
        val fileUri = fd.fileUri
        if (fileUri != null) {
            optional.addProperty("name", getFileName(fileUri))
            optional.addProperty("size", getFileSize(fileUri))
        }
        optional.addProperty("lastModified", 0)
        return attachments
    }

    private fun attachmentsFromMfmsPath(fileDescription: FileDescription?): JsonArray {
        val attachments = JsonArray()
        val attachment = JsonObject()
        attachments.add(attachment)
        val optional = JsonObject()
        attachment.addProperty("isSelfie", fileDescription!!.isSelfie)
        attachment.add("optional", optional)
        if (fileDescription.incomingName != null) {
            optional.addProperty(
                MessageAttributes.TYPE,
                getMimeType(
                    Uri.parse(
                        fileDescription.incomingName
                    )
                )
            )
        }
        optional.addProperty("name", fileDescription.incomingName)
        optional.addProperty("size", fileDescription.size)
        optional.addProperty("lastModified", System.currentTimeMillis())
        return attachments
    }

    private fun attachmentsFromFileDescription(
        fileDescription: FileDescription?,
        mfmsFilepath: String
    ): JsonArray? {
        var attachments: JsonArray? = null
        if (fileDescription!!.fileUri != null) {
            attachments = attachmentsFromFileDescription(fileDescription, fileDescription.isSelfie)
        } else if (fileDescription.downloadPath != null) {
            attachments = attachmentsFromMfmsPath(fileDescription)
        }
        if (attachments != null) {
            (attachments[0] as JsonObject).addProperty("result", mfmsFilepath)
        }
        return attachments
    }
}
