package im.threads.business.transport

import android.net.Uri
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import im.threads.business.UserInfoBuilder
import im.threads.business.formatters.ChatItemType
import im.threads.business.models.CAMPAIGN_DATE_FORMAT
import im.threads.business.models.ConsultInfo
import im.threads.business.models.FileDescription
import im.threads.business.models.Survey
import im.threads.business.models.UserPhrase
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.rest.queries.ThreadsApi
import im.threads.business.utils.AppInfoHelper
import im.threads.business.utils.DeviceInfoHelper
import im.threads.business.utils.FileUtils.getFileName
import im.threads.business.utils.FileUtils.getFileSize
import im.threads.business.utils.FileUtils.getMimeType
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class OutgoingMessageCreator(private val preferences: Preferences) {
    fun createInitChatMessage(): JsonObject {
        val jsonObject = JsonObject()
        val userInfo = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)
        jsonObject.apply {
            addProperty(MessageAttributes.CLIENT_ID, userInfo?.clientId)
            addProperty(MessageAttributes.TYPE, ChatItemType.INIT_CHAT.name)
            addProperty(MessageAttributes.DATA, userInfo?.clientData)
            addProperty(MessageAttributes.APP_MARKER_KEY, userInfo?.appMarker)
            addProperty(MessageAttributes.AUTHORIZED, true)
        }
        return jsonObject
    }

    fun createEnvironmentMessage(locale: String): JsonObject {
        val userInfo = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)
        val jsonObject = JsonObject().apply {
            addProperty("name", userInfo?.userName)
            addProperty(MessageAttributes.CLIENT_ID, userInfo?.clientId)
            addProperty(MessageAttributes.CLIENT_ID_ENCRYPTED, userInfo?.clientIdEncrypted)
            userInfo?.clientData?.let { addProperty(MessageAttributes.DATA, it) }
            addProperty("platform", "Android")
            addProperty("osVersion", DeviceInfoHelper.getOsVersion())
            addProperty("device", DeviceInfoHelper.getDeviceName())
            addProperty("ip", DeviceInfoHelper.getIpAddress())
            addProperty("appVersion", AppInfoHelper.getAppVersion())
            addProperty("appName", AppInfoHelper.getAppName())
            addProperty(MessageAttributes.APP_BUNDLE_KEY, AppInfoHelper.getAppId())
            addProperty(MessageAttributes.APP_MARKER_KEY, userInfo?.appMarker)
            addProperty("libVersion", AppInfoHelper.getLibVersion())
            addProperty("clientLocale", locale)
            addProperty("chatApiVersion", ThreadsApi.API_VERSION)
            addProperty(MessageAttributes.TYPE, ChatItemType.CLIENT_INFO.name)
            addProperty(MessageAttributes.AUTHORIZED, true)
        }
        return jsonObject
    }

    fun createMessageTyping(input: String?): JsonObject {
        val userInfo = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)
        val jsonObject = JsonObject().apply {
            addProperty(MessageAttributes.CLIENT_ID, userInfo?.clientId)
            addProperty(MessageAttributes.TYPE, ChatItemType.TYPING.name)
            addProperty(MessageAttributes.TYPING_DRAFT, input)
            addProperty(MessageAttributes.APP_MARKER_KEY, userInfo?.appMarker)
            addProperty(MessageAttributes.AUTHORIZED, true)
        }
        return jsonObject
    }

    fun createRatingDoneMessage(survey: Survey): JsonObject {
        val userInfo = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)
        val jsonObject = JsonObject().apply {
            addProperty(MessageAttributes.CLIENT_ID, userInfo?.clientId)
            addProperty(MessageAttributes.TYPE, ChatItemType.SURVEY_QUESTION_ANSWER.name)
            addProperty("sendingId", survey.sendingId)
            addProperty("questionId", survey.questions[0].id)
            addProperty("rate", survey.questions[0].rate)
            addProperty("text", survey.questions[0].text)
            addProperty(MessageAttributes.APP_MARKER_KEY, userInfo?.appMarker)
            addProperty(MessageAttributes.AUTHORIZED, true)
        }
        return jsonObject
    }

    fun createResolveThreadMessage(): JsonObject {
        val userInfo = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)
        val jsonObject = JsonObject().apply {
            addProperty(MessageAttributes.CLIENT_ID, userInfo?.clientId)
            addProperty(MessageAttributes.TYPE, ChatItemType.CLOSE_THREAD.name)
            addProperty(MessageAttributes.APP_MARKER_KEY, userInfo?.appMarker)
            addProperty(MessageAttributes.AUTHORIZED, true)
        }
        return jsonObject
    }

    fun createReopenThreadMessage(): JsonObject {
        val userInfo = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)
        val jsonObject = JsonObject().apply {
            addProperty(MessageAttributes.CLIENT_ID, userInfo?.clientId)
            addProperty(MessageAttributes.TYPE, ChatItemType.REOPEN_THREAD.name)
            addProperty(MessageAttributes.APP_MARKER_KEY, userInfo?.appMarker)
            addProperty(MessageAttributes.AUTHORIZED, true)
        }
        return jsonObject
    }

    fun createMessageClientOffline(clientId: String?): JsonObject {
        val userInfo = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)
        val jsonObject = JsonObject().apply {
            addProperty(MessageAttributes.CLIENT_ID, clientId)
            addProperty(MessageAttributes.TYPE, ChatItemType.CLIENT_OFFLINE.name)
            addProperty(MessageAttributes.APP_MARKER_KEY, userInfo?.appMarker)
            addProperty(MessageAttributes.AUTHORIZED, true)
        }
        return jsonObject
    }

    fun createMessageUpdateLocation(
        latitude: Double,
        longitude: Double,
        locale: String
    ): JsonObject {
        val userInfo = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)
        val jsonObject = JsonObject().apply {
            val location = JsonObject().apply {
                addProperty(MessageAttributes.COORDINATES, "$latitude, $longitude")
            }
            addProperty(MessageAttributes.DATA, location.toString())
            addProperty("name", userInfo?.userName)
            addProperty(MessageAttributes.CLIENT_ID, userInfo?.clientId)
            addProperty(MessageAttributes.CLIENT_ID_ENCRYPTED, userInfo?.clientIdEncrypted)
            addProperty("platform", "Android")
            addProperty("osVersion", DeviceInfoHelper.getOsVersion())
            addProperty("device", DeviceInfoHelper.getDeviceName())
            addProperty("ip", DeviceInfoHelper.getIpAddress())
            addProperty("appVersion", AppInfoHelper.getAppVersion())
            addProperty("appName", AppInfoHelper.getAppName())
            addProperty(MessageAttributes.APP_BUNDLE_KEY, AppInfoHelper.getAppId())
            addProperty(MessageAttributes.APP_MARKER_KEY, userInfo?.appMarker)
            addProperty("libVersion", AppInfoHelper.getLibVersion())
            addProperty("clientLocale", locale)
            addProperty("chatApiVersion", ThreadsApi.API_VERSION)
            addProperty(MessageAttributes.TYPE, ChatItemType.UPDATE_LOCATION.name)
            addProperty(MessageAttributes.AUTHORIZED, true)
        }
        return jsonObject
    }

    fun createUserPhraseMessage(
        userPhrase: UserPhrase,
        consultInfo: ConsultInfo?,
        quoteMfmsFilePath: String?,
        mfmsFilePath: String?
    ): JsonObject {
        val phrase = userPhrase.phraseText
        val quote = userPhrase.quote
        val fileDescription = userPhrase.fileDescription
        val campaignMessage = userPhrase.campaignMessage
        val userInfo = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)
        val formattedMessage = JsonObject().apply {
            addProperty(MessageAttributes.UUID, userPhrase.id)
            addProperty(MessageAttributes.CLIENT_ID, userInfo?.clientId)
            addProperty(MessageAttributes.TEXT, phrase ?: "")
            addProperty(MessageAttributes.APP_MARKER_KEY, userInfo?.appMarker)
            addProperty(MessageAttributes.AUTHORIZED, true)
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
        fileDescription: FileDescription?
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
                attachments = attachmentsFromFileDescription(file)
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
