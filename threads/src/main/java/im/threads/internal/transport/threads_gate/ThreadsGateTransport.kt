package im.threads.internal.transport.threads_gate

import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.core.util.ObjectsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import im.threads.config.SocketClientSettings
import im.threads.internal.Config
import im.threads.internal.chat_updates.ChatUpdateProcessor
import im.threads.internal.domain.logger.LoggerEdna
import im.threads.internal.formatters.ChatItemType
import im.threads.internal.model.CampaignMessage
import im.threads.internal.model.ChatItemSendErrorModel
import im.threads.internal.model.ConsultInfo
import im.threads.internal.model.SpeechMessageUpdate
import im.threads.internal.model.SslSocketFactoryConfig
import im.threads.internal.model.Survey
import im.threads.internal.model.UserPhrase
import im.threads.internal.transport.ApplicationConfig
import im.threads.internal.transport.AuthInterceptor
import im.threads.internal.transport.ChatItemProviderData
import im.threads.internal.transport.MessageAttributes
import im.threads.internal.transport.MessageAttributes.ATTACHMENTS
import im.threads.internal.transport.OutgoingMessageCreator
import im.threads.internal.transport.Transport
import im.threads.internal.transport.TransportException
import im.threads.internal.transport.models.Attachment
import im.threads.internal.transport.models.AttachmentSettings
import im.threads.internal.transport.models.TypingContent
import im.threads.internal.transport.threads_gate.requests.RegisterDeviceRequest
import im.threads.internal.transport.threads_gate.requests.SendMessageRequest
import im.threads.internal.transport.threads_gate.responses.BaseResponse
import im.threads.internal.transport.threads_gate.responses.GetMessagesData
import im.threads.internal.transport.threads_gate.responses.GetStatusesData
import im.threads.internal.transport.threads_gate.responses.RegisterDeviceData
import im.threads.internal.transport.threads_gate.responses.SendMessageData
import im.threads.internal.utils.AppInfoHelper
import im.threads.internal.utils.DeviceInfoHelper
import im.threads.internal.utils.PrefUtils
import im.threads.internal.utils.SSLCertificateInterceptor
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
    threadsGateHuaweiProviderUid: String?,
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

    init {
        val httpClientBuilder = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
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
            httpClientBuilder.addInterceptor(SSLCertificateInterceptor())
        }
        if (sslSocketFactoryConfig != null) {
            httpClientBuilder.sslSocketFactory(
                sslSocketFactoryConfig.sslSocketFactory,
                sslSocketFactoryConfig.trustManager
            )
            httpClientBuilder.hostnameVerifier { hostname: String, session: SSLSession -> true }
        }
        client = httpClientBuilder.build()
        request = Request.Builder()
            .url(threadsGateUrl)
            .build()
        listener = WebSocketListener()
        applicationConfig = ApplicationConfig(threadsGateProviderUid, threadsGateHuaweiProviderUid)
    }

    override fun init() {}
    override fun sendRatingDone(survey: Survey) {
        val content = OutgoingMessageCreator.createRatingDoneMessage(
            survey,
            PrefUtils.clientID,
            PrefUtils.appMarker
        )
        surveysInProcess[survey.sendingId] = survey
        sendMessage(
            content,
            true,
            ChatItemType.SURVEY_QUESTION_ANSWER.name + CORRELATION_ID_DIVIDER + survey.sendingId
        )
    }

    override fun sendResolveThread(approveResolve: Boolean) {
        val clientID = PrefUtils.clientID
        val content: JsonObject
        var correlationId: String
        if (approveResolve) {
            content = OutgoingMessageCreator.createResolveThreadMessage(clientID)
            correlationId = ChatItemType.CLOSE_THREAD.name
        } else {
            content = OutgoingMessageCreator.createReopenThreadMessage(clientID)
            correlationId = ChatItemType.REOPEN_THREAD.name
        }
        correlationId += CORRELATION_ID_DIVIDER + UUID.randomUUID().toString()
        sendMessage(content, false, correlationId)
    }

    override fun sendUserTying(input: String) {
        sendMessage(OutgoingMessageCreator.createMessageTyping(PrefUtils.clientID, input))
    }

    override fun sendInit() {
        if (!TextUtils.isEmpty(PrefUtils.deviceAddress)) {
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
        LoggerEdna.info("sendMessage: userPhrase = $userPhrase, consultInfo = $consultInfo, filePath = $filePath, quoteFilePath = $quoteFilePath")
        userPhrase.campaignMessage?.let {
            campaignsInProcess[userPhrase.id] = it
        }
        val content = OutgoingMessageCreator.createUserPhraseMessage(
            userPhrase,
            consultInfo,
            quoteFilePath,
            filePath,
            PrefUtils.clientID
        )
        sendMessage(
            content,
            true,
            ChatItemType.MESSAGE.name + CORRELATION_ID_DIVIDER + userPhrase.id
        )
    }

    override fun sendClientOffline(clientId: String) {
        if (TextUtils.isEmpty(PrefUtils.deviceAddress)) {
            return
        }
        val content = OutgoingMessageCreator.createMessageClientOffline(
            clientId
        )
        sendMessage(content, sendInit = false)
    }

    override fun getToken(): String {
        val clientIdSignature = PrefUtils.clientIdSignature
        return (
            (if (TextUtils.isEmpty(clientIdSignature)) PrefUtils.deviceAddress else clientIdSignature) +
                ":" + PrefUtils.clientID
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
        LoggerEdna.info("sendMessage: content = $content, important = $important, correlationId = $correlationId")
        synchronized(messageInProcessIds) {
            messageInProcessIds.add(correlationId)
        }
        if (webSocket == null && tryOpeningWebSocket) {
            openWebSocket()
        }
        val ws = webSocket ?: return
        if (sendInit &&
            !TextUtils.isEmpty(PrefUtils.clientID) &&
            !TextUtils.isEmpty(PrefUtils.deviceAddress)
        ) {
            sendInitChatMessage(false)
            sendEnvironmentMessage(false)
        }
        val text = Config.instance.gson.toJson(
            SendMessageRequest(
                correlationId,
                SendMessageRequest.Data(PrefUtils.deviceAddress, content, important)
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
        val deviceModel = Build.MANUFACTURER + ' ' + Build.MODEL
        val deviceName =
            Settings.Secure.getString(Config.instance.context.contentResolver, "bluetooth_name")
        val cloudPair = applicationConfig.getCloudPair()
        val data = RegisterDeviceRequest.Data(
            AppInfoHelper.getAppId(),
            AppInfoHelper.getAppVersion(),
            cloudPair.providerUid,
            cloudPair.token,
            PrefUtils.deviceUid,
            "Android",
            DeviceInfoHelper.getOsVersion(),
            DeviceInfoHelper.getLocale(Config.instance.context),
            Calendar.getInstance().timeZone.displayName,
            if (!TextUtils.isEmpty(deviceName)) deviceName else deviceModel,
            deviceModel,
            PrefUtils.deviceAddress
        )
        val text = Config.instance.gson.toJson(
            RegisterDeviceRequest(UUID.randomUUID().toString(), data)
        )
        LoggerEdna.info("Sending : $text")
        ws.send(text)
    }

    private fun sendInitChatMessage(tryOpeningWebSocket: Boolean) {
        sendMessage(
            content = OutgoingMessageCreator.createInitChatMessage(
                PrefUtils.clientID,
                PrefUtils.data
            ),
            tryOpeningWebSocket = tryOpeningWebSocket,
            sendInit = false
        )
    }

    private fun sendEnvironmentMessage(tryOpeningWebSocket: Boolean) {
        sendMessage(
            OutgoingMessageCreator.createEnvironmentMessage(
                PrefUtils.userName,
                PrefUtils.clientID,
                PrefUtils.clientIDEncrypted,
                PrefUtils.data,
                Config.instance.context
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
                ChatUpdateProcessor.getInstance()
                    .postChatItemSendError(ChatItemSendErrorModel(userPhraseUuid = tokens[1]))
            }
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
            ChatUpdateProcessor.getInstance().postSocketResponseMap(socketResponseMap)
            LoggerEdna.info("OnOpen : $response")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            LoggerEdna.info("Receiving : $text")
            postSocketResponseMap(text)
            val response = Config.instance.gson.fromJson(text, BaseResponse::class.java)
            LoggerEdna.error("Receiving : daseResponse ${response.data}")
            val action = response.action

            if (response.data.has(KEY_ERROR)) {
                ChatUpdateProcessor.getInstance()
                    .postError(TransportException(response.data[KEY_ERROR].asString))
            } else if (action != null) {
                if (action == Action.REGISTER_DEVICE) {
                    val data = Config.instance.gson.fromJson(
                        response.data.toString(),
                        RegisterDeviceData::class.java
                    )
                    val initialRegistration = TextUtils.isEmpty(PrefUtils.deviceAddress)
                    PrefUtils.deviceAddress = data.deviceAddress
                    if (initialRegistration) {
                        sendInitChatMessage(false)
                        sendEnvironmentMessage(false)
                    }
                }
                if (action == Action.SEND_MESSAGE) {
                    val data = Config.instance.gson.fromJson(
                        response.data.toString(),
                        SendMessageData::class.java
                    )
                    val tokens = response.correlationId.split(CORRELATION_ID_DIVIDER).toTypedArray()
                    if (tokens.size > 1) {
                        when (ChatItemType.fromString(tokens[0])) {
                            ChatItemType.MESSAGE -> {
                                if (campaignsInProcess.containsKey(tokens[1])) {
                                    val campaignMessage = campaignsInProcess[tokens[1]]
                                    ChatUpdateProcessor.getInstance()
                                        .postCampaignMessageReplySuccess(campaignMessage)
                                    campaignsInProcess.remove(tokens[1])
                                }
                                ChatUpdateProcessor.getInstance()
                                    .postChatItemSendSuccess(
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
                                    ChatUpdateProcessor.getInstance().postSurveySendSuccess(survey)
                                    surveysInProcess.remove(sendingId)
                                }
                            }
                            ChatItemType.REOPEN_THREAD, ChatItemType.CLOSE_THREAD -> ChatUpdateProcessor.getInstance()
                                .postRemoveChatItem(ChatItemType.REQUEST_CLOSE_THREAD)
                            else -> {
                            }
                        }
                    }
                }
                if (action == Action.GET_STATUSES) {
                    val data = Config.instance.gson.fromJson(
                        response.data.toString(),
                        GetStatusesData::class.java
                    )
                    for (status in data.statuses) {
                        if (ObjectsCompat.equals(MessageStatus.READ, status.status)) {
                            ChatUpdateProcessor.getInstance()
                                .postOutgoingMessageWasRead(status.messageId)
                        }
                    }
                }
                if (action == Action.GET_MESSAGES) {
                    val data = Config.instance.gson.fromJson(
                        response.data.toString(),
                        GetMessagesData::class.java
                    )
                    for (message in data.messages) {
                        if (message.content.has(MessageAttributes.TYPE)) {
                            val type =
                                ChatItemType.fromString(ThreadsGateMessageParser.getType(message))
                            if (ChatItemType.TYPING == type) {
                                val content = Config.instance.gson.fromJson(
                                    message.content,
                                    TypingContent::class.java
                                )
                                ChatUpdateProcessor.getInstance().postTyping(content.clientId)
                            } else if (ChatItemType.ATTACHMENT_SETTINGS == type) {
                                val attachmentSettings = Config.instance.gson.fromJson(
                                    message.content,
                                    AttachmentSettings::class.java
                                )
                                ChatUpdateProcessor.getInstance()
                                    .postAttachmentSettings(attachmentSettings)
                            } else if (ChatItemType.ATTACHMENT_UPDATED == type) {
                                val attachments: ArrayList<Attachment> = ArrayList()
                                (message.content.get(ATTACHMENTS) as JsonArray)?.let {
                                    for (i in 0 until it.size()) {
                                        attachments.add(
                                            Config.instance.gson.fromJson(
                                                it[i],
                                                Attachment::class.java
                                            )
                                        )
                                    }
                                }
                                if (attachments.isNotEmpty()) {
                                    ChatUpdateProcessor.getInstance().updateAttachments(attachments)
                                }
                            } else if (ChatItemType.SPEECH_MESSAGE_UPDATED == type) {
                                val chatItem = ThreadsGateMessageParser.format(message)
                                if (chatItem is SpeechMessageUpdate) {
                                    ChatUpdateProcessor.getInstance()
                                        .postSpeechMessageUpdate(chatItem)
                                }
                            } else {
                                val chatItem = ThreadsGateMessageParser.format(message)
                                if (chatItem != null) {
                                    ChatUpdateProcessor.getInstance().postNewMessage(chatItem)
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
            ChatUpdateProcessor.getInstance().postSocketResponseMap(socketResponseMap)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            LoggerEdna.info("Receiving bytes : " + bytes.hex())
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            postSocketResponseMap(code, reason)
            LoggerEdna.info("Closing : $code / $reason")
            webSocket.close(Companion.NORMAL_CLOSURE_STATUS, null)
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
            ChatUpdateProcessor.getInstance().postSocketResponseMap(socketResponseMap)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            LoggerEdna.info("Error : " + t.message)
            ChatUpdateProcessor.getInstance().postError(TransportException(t.message))
            synchronized(messageInProcessIds) {
                for (messageId in messageInProcessIds) {
                    processMessageSendError(messageId)
                }
                messageInProcessIds.clear()
            }
            closeWebSocket()
        }

        @Throws(JSONException::class)
        private fun JSONObject.toMap(): Map<String, Any> {
            val map = mutableMapOf<String, Any>()
            for (key in this.keys()) {
                var value: Any = this.get(key)
                when (value) {
                    is JSONArray -> value = value.toList()
                    is JSONObject -> value = value.toMap()
                }
                map[key] = value
            }
            return map
        }

        @Throws(JSONException::class)
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
