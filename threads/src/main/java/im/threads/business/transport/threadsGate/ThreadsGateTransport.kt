package im.threads.business.transport.threadsGate

import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.core.util.ObjectsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import im.threads.business.UserInfoBuilder
import im.threads.business.chat_updates.ChatUpdateProcessor
import im.threads.business.config.BaseConfig
import im.threads.business.formatters.ChatItemType
import im.threads.business.logger.LoggerEdna
import im.threads.business.models.CampaignMessage
import im.threads.business.models.ChatItemSendErrorModel
import im.threads.business.models.ConsultInfo
import im.threads.business.models.SpeechMessageUpdate
import im.threads.business.models.SslSocketFactoryConfig
import im.threads.business.models.Survey
import im.threads.business.models.UserPhrase
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.rest.config.SocketClientSettings
import im.threads.business.serviceLocator.core.inject
import im.threads.business.transport.ApplicationConfig
import im.threads.business.transport.AuthInterceptor
import im.threads.business.transport.ChatItemProviderData
import im.threads.business.transport.MessageAttributes
import im.threads.business.transport.MessageAttributes.ATTACHMENTS
import im.threads.business.transport.OutgoingMessageCreator
import im.threads.business.transport.Transport
import im.threads.business.transport.TransportException
import im.threads.business.transport.models.Attachment
import im.threads.business.transport.models.AttachmentSettings
import im.threads.business.transport.models.TypingContent
import im.threads.business.transport.threadsGate.requests.RegisterDeviceRequest
import im.threads.business.transport.threadsGate.requests.SendMessageRequest
import im.threads.business.transport.threadsGate.responses.BaseResponse
import im.threads.business.transport.threadsGate.responses.GetMessagesData
import im.threads.business.transport.threadsGate.responses.GetStatusesData
import im.threads.business.transport.threadsGate.responses.RegisterDeviceData
import im.threads.business.transport.threadsGate.responses.SendMessageData
import im.threads.business.utils.AppInfoHelper
import im.threads.business.utils.DeviceInfoHelper
import im.threads.business.utils.SSLCertificateInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.logging.HttpLoggingInterceptor
import okio.ByteString
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSession

class ThreadsGateTransport(
    threadsGateUrl: String,
    threadsGateProviderUid: String,
    isDebugLoggingEnabled: Boolean,
    socketSettings: SocketClientSettings,
    sslSocketFactoryConfig: SslSocketFactoryConfig? = null,
    networkInterceptor: Interceptor?
) : Transport(), LifecycleObserver {
    private val client: OkHttpClient
    private val request: Request
    private val listener: WebSocketListener
    private val applicationConfig: ApplicationConfig
    private val messageInProcessIds: MutableList<String> = ArrayList()
    private val surveysInProcess: MutableMap<Long, Survey> = HashMap()
    private val campaignsInProcess: MutableMap<String?, CampaignMessage> = HashMap()
    private var webSocket: WebSocket? = null
    private var lifecycle: Lifecycle? = null
    private val outgoingMessageCreator: OutgoingMessageCreator by inject()
    private val preferences: Preferences by inject()
    private val authInterceptor: AuthInterceptor by inject()
    private val chatUpdateProcessor: ChatUpdateProcessor by inject()

    init {
        val httpClientBuilder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .apply { networkInterceptor?.let { addInterceptor(it) } }
            .pingInterval(socketSettings.resendPingIntervalMillis.toLong(), TimeUnit.MILLISECONDS)
            .connectTimeout(socketSettings.connectTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(socketSettings.readTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(socketSettings.writeTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
        if (isDebugLoggingEnabled) {
            httpClientBuilder.addInterceptor(
                HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY)
            )
        }
        if (sslSocketFactoryConfig != null) {
            if (isDebugLoggingEnabled) {
                httpClientBuilder.addInterceptor(SSLCertificateInterceptor())
            }

            httpClientBuilder.sslSocketFactory(
                sslSocketFactoryConfig.sslSocketFactory,
                sslSocketFactoryConfig.trustManager
            )
            httpClientBuilder.hostnameVerifier { _: String, _: SSLSession -> true }
        }
        client = httpClientBuilder.build()
        request = Request.Builder()
            .url(threadsGateUrl)
            .build()
        listener = WebSocketListener()
        applicationConfig = ApplicationConfig(
            threadsGateProviderUid,
            preferences
        )
    }

    override fun init() {}
    override fun sendRatingDone(survey: Survey) {
        val content = outgoingMessageCreator.createRatingDoneMessage(survey)
        surveysInProcess[survey.sendingId] = survey
        sendMessage(
            content,
            true,
            ChatItemType.SURVEY_QUESTION_ANSWER.name + CORRELATION_ID_DIVIDER + survey.sendingId
        )
    }

    override fun sendResolveThread(approveResolve: Boolean) {
        val content: JsonObject
        var correlationId: String
        if (approveResolve) {
            content = outgoingMessageCreator.createResolveThreadMessage()
            correlationId = ChatItemType.CLOSE_THREAD.name
        } else {
            content = outgoingMessageCreator.createReopenThreadMessage()
            correlationId = ChatItemType.REOPEN_THREAD.name
        }
        correlationId += CORRELATION_ID_DIVIDER + UUID.randomUUID().toString()
        sendMessage(content, false, correlationId)
    }

    override fun sendUserTying(input: String) {
        sendMessage(outgoingMessageCreator.createMessageTyping(input))
    }

    override fun sendInit() {
        val deviceAddress = preferences.get<String>(PreferencesCoreKeys.DEVICE_ADDRESS)
        if (!TextUtils.isEmpty(deviceAddress)) {
            sendInitChatMessage(true)
            sendEnvironmentMessage(true)
        } else {
            openWebSocket()
        }
    }

    override fun sendMessage(
        userPhrase: UserPhrase,
        consultInfo: ConsultInfo?,
        filePath: String?,
        quoteFilePath: String?
    ) {
        LoggerEdna.info(
            "sendMessage: userPhrase = $userPhrase, consultInfo = $consultInfo, filePath = $filePath, quoteFilePath = $quoteFilePath"
        )
        userPhrase.campaignMessage?.let {
            campaignsInProcess[userPhrase.id] = it
        }
        val content = outgoingMessageCreator.createUserPhraseMessage(
            userPhrase,
            consultInfo,
            quoteFilePath,
            filePath
        )
        sendMessage(
            content,
            true,
            ChatItemType.MESSAGE.name + CORRELATION_ID_DIVIDER + userPhrase.id
        )
    }

    override fun sendClientOffline(clientId: String) {
        if (preferences.get<String>(PreferencesCoreKeys.DEVICE_ADDRESS).isNullOrBlank()) {
            return
        }
        val content = outgoingMessageCreator.createMessageClientOffline(clientId)
        sendMessage(content, sendInit = false)
    }

    override fun updateLocation(latitude: Double, longitude: Double) {
        val content = outgoingMessageCreator.createMessageUpdateLocation(
            latitude,
            longitude,
            DeviceInfoHelper.getLocale(BaseConfig.instance.context)
        )
        sendMessage(content, sendInit = false)
    }

    override fun getToken(): String {
        val userInfo = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)
        val deviceAddress = preferences.get<String>(PreferencesCoreKeys.DEVICE_ADDRESS)
        return (
            (if (userInfo?.clientIdSignature.isNullOrEmpty()) deviceAddress else userInfo?.clientIdSignature) +
                ":" + userInfo?.clientId
            )
    }

    @Synchronized
    override fun setLifecycle(lifecycle: Lifecycle?) {
        lifecycle?.apply {
            removeObserver(this@ThreadsGateTransport)
        }
        this.lifecycle = lifecycle
        lifecycle?.apply {
            addObserver(this@ThreadsGateTransport)
        }
    }

    private fun sendMessage(
        content: JsonObject,
        important: Boolean = false,
        correlationId: String = UUID.randomUUID().toString(),
        tryOpeningWebSocket: Boolean = true,
        sendInit: Boolean = true
    ) {
        LoggerEdna.info(
            "sendMessage: content = $content, important = $important, correlationId = $correlationId"
        )
        synchronized(messageInProcessIds) {
            messageInProcessIds.add(correlationId)
        }
        if (webSocket == null && tryOpeningWebSocket) {
            openWebSocket()
        }
        val ws = webSocket ?: return
        val clientId = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)?.clientId
        val deviceAddress = preferences.get<String>(PreferencesCoreKeys.DEVICE_ADDRESS)
        if (sendInit && !clientId.isNullOrBlank() && !deviceAddress.isNullOrBlank()) {
            sendInitChatMessage(false)
            sendEnvironmentMessage(false)
        }
        val text = BaseConfig.instance.gson.toJson(
            SendMessageRequest(
                correlationId,
                SendMessageRequest.Data(deviceAddress, content, important)
            )
        )
        LoggerEdna.info("Sending : $text")
        ws.send(text)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    @Synchronized
    private fun openWebSocket() {
        if (webSocket == null) {
            webSocket = client.newWebSocket(request, listener)
            sendRegisterDevice()
        }
    }

    private fun sendRegisterDevice() {
        val ws = webSocket ?: return
        val deviceModel = getSimpleDeviceName()
        val deviceName = getDeviceName()
        val deviceAddress = preferences.get<String>(PreferencesCoreKeys.DEVICE_ADDRESS)
        val cloudPair = applicationConfig.getCloudPair()
        val data = RegisterDeviceRequest.Data(
            AppInfoHelper.getAppId(),
            AppInfoHelper.getAppVersion(),
            cloudPair.providerUid,
            cloudPair.token,
            getDeviceUid(),
            "Android",
            DeviceInfoHelper.getOsVersion(),
            DeviceInfoHelper.getLocale(BaseConfig.instance.context),
            Calendar.getInstance().timeZone.displayName,
            if (!TextUtils.isEmpty(deviceName)) deviceName else deviceModel,
            deviceModel,
            deviceAddress
        )
        val text = BaseConfig.instance.gson.toJson(
            RegisterDeviceRequest(UUID.randomUUID().toString(), data)
        )
        LoggerEdna.info("Sending : $text")
        ws.send(text)
    }

    @Synchronized
    private fun getDeviceUid(): String {
        var deviceUid = preferences.get<String>(PreferencesCoreKeys.DEVICE_UID)
        if (deviceUid.isNullOrBlank()) {
            deviceUid = UUID.randomUUID().toString()
            preferences.save(PreferencesCoreKeys.DEVICE_UID, deviceUid)
        }

        return deviceUid
    }

    private fun sendInitChatMessage(tryOpeningWebSocket: Boolean) {
        sendMessage(
            content = outgoingMessageCreator.createInitChatMessage(),
            tryOpeningWebSocket = tryOpeningWebSocket,
            sendInit = false
        )
    }

    private fun sendEnvironmentMessage(tryOpeningWebSocket: Boolean) {
        sendMessage(
            outgoingMessageCreator.createEnvironmentMessage(
                DeviceInfoHelper.getLocale(BaseConfig.instance.context)
            ),
            tryOpeningWebSocket = tryOpeningWebSocket,
            sendInit = false
        )
    }

    /**
     * Closes websocket connection if there are no messages left to send
     * and user is not interacting with chat screen
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun closeWebSocketIfNeeded() {
        if (messageInProcessIds.isEmpty() &&
            (lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED)?.not() != false)
        ) {
            closeWebSocket()
        }
    }

    @Synchronized
    private fun closeWebSocket() {
        webSocket?.apply {
            close(NORMAL_CLOSURE_STATUS, null)
        }
        webSocket = null
    }

    private fun processMessageSendError(correlationId: String) {
        val tokens = correlationId.split(CORRELATION_ID_DIVIDER).toTypedArray()
        if (tokens.size > 1) {
            val type = ChatItemType.fromString(tokens[0])
            if (type == ChatItemType.MESSAGE) {
                chatUpdateProcessor.postChatItemSendError(ChatItemSendErrorModel(userPhraseUuid = tokens[1]))
            }
        }
    }

    private fun getDeviceName(): String {
        val deviceName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            Settings.Secure.getString(
                BaseConfig.instance.context.contentResolver,
                Settings.Global.DEVICE_NAME
            )
        } else null

        return if (deviceName.isNullOrBlank() && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            val blName = try {
                Settings.Secure.getString(BaseConfig.instance.context.contentResolver, "bluetooth_name")
            } catch (ignored: Exception) {
                getSimpleDeviceName()
            }
            blName ?: getSimpleDeviceName()
        } else if (deviceName.isNullOrBlank()) {
            getSimpleDeviceName()
        } else {
            deviceName
        }
    }

    private fun getSimpleDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            model
        } else {
            "${manufacturer.capitalize()} $model"
        }
    }

    private inner class WebSocketListener : okhttp3.WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            val socketResponseMap = mutableMapOf<String, Any>().apply {
                put(KEY_PROTOCOL, response.protocol)
                put(KEY_CODE, response.code)
                put(KEY_MESSAGE, response.message)
                put(KEY_URL, response.request.url)
            }
            chatUpdateProcessor.postSocketResponseMap(socketResponseMap)
            LoggerEdna.info("OnOpen : $response")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            LoggerEdna.info("Receiving : $text")
            postSocketResponseMap(text)
            val response = BaseConfig.instance.gson.fromJson(text, BaseResponse::class.java)
            LoggerEdna.error("Receiving : daseResponse ${response.data}")
            val action = response.action

            if (response.data.has(KEY_ERROR)) {
                chatUpdateProcessor.postError(TransportException(response.data[KEY_ERROR].asString))
            } else if (action != null) {
                if (action == Action.REGISTER_DEVICE) {
                    val data = BaseConfig.instance.gson.fromJson(
                        response.data.toString(),
                        RegisterDeviceData::class.java
                    )
                    val initialRegistration = preferences
                        .get<String>(PreferencesCoreKeys.DEVICE_ADDRESS).isNullOrBlank()
                    preferences.save(PreferencesCoreKeys.DEVICE_ADDRESS, data.deviceAddress)
                    if (initialRegistration) {
                        sendInitChatMessage(false)
                        sendEnvironmentMessage(false)
                    }
                }
                if (action == Action.SEND_MESSAGE) {
                    val data = BaseConfig.instance.gson.fromJson(
                        response.data.toString(),
                        SendMessageData::class.java
                    )
                    val tokens = response.correlationId.split(CORRELATION_ID_DIVIDER).toTypedArray()
                    if (tokens.size > 1) {
                        when (ChatItemType.fromString(tokens[0])) {
                            ChatItemType.MESSAGE -> {
                                if (campaignsInProcess.containsKey(tokens[1])) {
                                    val campaignMessage = campaignsInProcess[tokens[1]]
                                    chatUpdateProcessor.postCampaignMessageReplySuccess(campaignMessage)
                                    campaignsInProcess.remove(tokens[1])
                                }
                                chatUpdateProcessor.postChatItemSendSuccess(
                                    ChatItemProviderData(
                                        tokens[1],
                                        data.messageId,
                                        data.sentAt.time
                                    )
                                )
                            }
                            ChatItemType.SURVEY_QUESTION_ANSWER -> {
                                val sendingId = tokens[1].toLong()
                                if (surveysInProcess.containsKey(sendingId)) {
                                    val survey = surveysInProcess[sendingId]
                                    chatUpdateProcessor.postSurveySendSuccess(survey)
                                    surveysInProcess.remove(sendingId)
                                }
                            }
                            ChatItemType.REOPEN_THREAD, ChatItemType.CLOSE_THREAD ->
                                chatUpdateProcessor
                                    .postRemoveChatItem(ChatItemType.REQUEST_CLOSE_THREAD)
                            else -> {
                            }
                        }
                    }
                }
                if (action == Action.GET_STATUSES) {
                    val data = BaseConfig.instance.gson.fromJson(
                        response.data.toString(),
                        GetStatusesData::class.java
                    )
                    for (status in data.statuses) {
                        if (ObjectsCompat.equals(MessageStatus.READ, status.status)) {
                            chatUpdateProcessor.postOutgoingMessageWasRead(status.messageId)
                        }
                    }
                }
                if (action == Action.GET_MESSAGES) {
                    val data = BaseConfig.instance.gson.fromJson(
                        response.data.toString(),
                        GetMessagesData::class.java
                    )
                    val gson = BaseConfig.instance.gson
                    for (message in data.messages) {
                        if (message.content.has(MessageAttributes.TYPE)) {
                            val type =
                                ChatItemType.fromString(ThreadsGateMessageParser.getType(message))
                            if (ChatItemType.TYPING == type) {
                                val content = gson.fromJson(
                                    message.content,
                                    TypingContent::class.java
                                )
                                chatUpdateProcessor.postTyping(content.clientId)
                            } else if (ChatItemType.ATTACHMENT_SETTINGS == type) {
                                val attachmentSettings = gson.fromJson(
                                    message.content,
                                    AttachmentSettings::class.java
                                )
                                chatUpdateProcessor.postAttachmentSettings(attachmentSettings)
                            } else if (ChatItemType.ATTACHMENT_UPDATED == type) {
                                val attachments: ArrayList<Attachment> = ArrayList()
                                (message.content.get(ATTACHMENTS) as JsonArray).forEach {
                                    attachments.add(gson.fromJson(it, Attachment::class.java))
                                }
                                if (attachments.isNotEmpty()) {
                                    chatUpdateProcessor.updateAttachments(attachments)
                                }
                            } else if (ChatItemType.SPEECH_MESSAGE_UPDATED == type) {
                                val chatItem = ThreadsGateMessageParser.format(message)
                                if (chatItem is SpeechMessageUpdate) {
                                    chatUpdateProcessor
                                        .postSpeechMessageUpdate(chatItem)
                                }
                            } else {
                                val chatItem = ThreadsGateMessageParser.format(message)
                                if (chatItem != null) {
                                    chatUpdateProcessor.postNewMessage(chatItem)
                                }
                            }
                        }
                    }
                }
            }
            synchronized(messageInProcessIds) {
                messageInProcessIds.remove(response.correlationId)
            }
            closeWebSocketIfNeeded()
        }

        private fun postSocketResponseMap(text: String) {
            var socketResponseMap: Map<String, Any>
            try {
                socketResponseMap = JSONObject(text).toMap()
            } catch (exception: JSONException) {
                socketResponseMap = mutableMapOf()
                socketResponseMap[KEY_TEXT] = text
            }
            chatUpdateProcessor.postSocketResponseMap(socketResponseMap)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            LoggerEdna.info("Receiving bytes : " + bytes.hex())
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            postSocketResponseMap(code, reason)
            LoggerEdna.info("Closing : $code / $reason")
            webSocket.close(NORMAL_CLOSURE_STATUS, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            postSocketResponseMap(code, reason)
            LoggerEdna.info("OnClosed : $code / $reason")
        }

        private fun postSocketResponseMap(code: Int, reason: String) {
            val socketResponseMap = mutableMapOf<String, Any>()
            socketResponseMap[KEY_CODE] = code
            try {
                val reasonMap = JSONObject(reason).toMap()
                socketResponseMap[KEY_REASON] = reasonMap
            } catch (exception: JSONException) {
                socketResponseMap[KEY_REASON] = reason
            }
            chatUpdateProcessor.postSocketResponseMap(socketResponseMap)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            LoggerEdna.info("Error : " + t.message)
            chatUpdateProcessor.postError(TransportException(t.message))
            synchronized(messageInProcessIds) {
                for (messageId in messageInProcessIds) {
                    processMessageSendError(messageId)
                }
                messageInProcessIds.clear()
            }
            closeWebSocket()
        }

        private fun JSONObject.toMap(): Map<String, Any> {
            val map = mutableMapOf<String, Any>()
            this.keys().forEach { key ->
                var value: Any = this.get(key)
                when (value) {
                    is JSONArray -> value = value.toList()
                    is JSONObject -> value = value.toMap()
                }
                map[key] = value
            }
            return map
        }

        private fun JSONArray.toList(): List<Any> {
            val list = mutableListOf<Any>()
            for (i in 0 until this.length()) {
                var value: Any = this[i]
                when (value) {
                    is JSONArray -> value = value.toList()
                    is JSONObject -> value = value.toMap()
                }
                list.add(value)
            }
            return list
        }
    }

    companion object {
        const val NORMAL_CLOSURE_STATUS = 1000
        private const val CORRELATION_ID_DIVIDER = ":"

        private const val KEY_TEXT = "text"
        private const val KEY_ERROR = "error"
        private const val KEY_CODE = "code"
        private const val KEY_REASON = "reason"
        private const val KEY_PROTOCOL = "protocol"
        private const val KEY_MESSAGE = "message"
        private const val KEY_URL = "url"
    }
}
