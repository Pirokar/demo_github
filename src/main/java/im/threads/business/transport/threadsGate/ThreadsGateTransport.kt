package im.threads.business.transport.threadsGate

import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import im.threads.business.chatUpdates.ChatUpdateProcessor
import im.threads.business.config.BaseConfig
import im.threads.business.formatters.ChatItemType
import im.threads.business.formatters.JsonFormatter
import im.threads.business.logger.LoggerEdna
import im.threads.business.logger.NetworkLoggerInterceptor
import im.threads.business.models.CampaignMessage
import im.threads.business.models.ChatItem
import im.threads.business.models.ChatItemSendErrorModel
import im.threads.business.models.ConsultInfo
import im.threads.business.models.LatLng
import im.threads.business.models.MessageStatus
import im.threads.business.models.SpeechMessageUpdate
import im.threads.business.models.SslSocketFactoryConfig
import im.threads.business.models.Survey
import im.threads.business.models.UserPhrase
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.rest.config.SocketClientSettings
import im.threads.business.secureDatabase.DatabaseHolder
import im.threads.business.serviceLocator.core.inject
import im.threads.business.state.ChatState
import im.threads.business.state.ChatStateEnum
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
import im.threads.business.transport.threadsGate.responses.Status
import im.threads.business.utils.AppInfo
import im.threads.business.utils.ClientUseCase
import im.threads.business.utils.DeviceInfo
import im.threads.business.utils.SSLCertificateInterceptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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
    private val threadsGateUrl: String,
    private val threadsGateProviderUid: String,
    private val isDebugLoggingEnabled: Boolean,
    private val socketSettings: SocketClientSettings,
    private val sslSocketFactoryConfig: SslSocketFactoryConfig? = null,
    private val networkInterceptor: Interceptor?
) : Transport(), LifecycleObserver {
    private lateinit var client: OkHttpClient
    private lateinit var request: Request
    private lateinit var listener: WebSocketListener
    private var location: LatLng? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val messageInProcessIds: MutableList<String> = ArrayList()
    private val surveysInProcess: MutableMap<Long, Survey> = HashMap()
    private val campaignsInProcess: MutableMap<String?, CampaignMessage> = HashMap()
    private var webSocket: WebSocket? = null
    private var lifecycle: Lifecycle? = null
    private val outgoingMessageCreator: OutgoingMessageCreator by inject()
    private val preferences: Preferences by inject()
    private val authInterceptor: AuthInterceptor by inject()
    private val chatUpdateProcessor: ChatUpdateProcessor by inject()
    private val database: DatabaseHolder by inject()
    private val jsonFormatter: JsonFormatter by inject()
    private val clientUseCase: ClientUseCase by inject()
    private val messageParser: ThreadsGateMessageParser by inject()
    private val appInfo: AppInfo by inject()
    private val deviceInfo: DeviceInfo by inject()
    private val chatState: ChatState by inject()

    init { buildTransport() }

    override fun buildTransport() {
        val httpClientBuilder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .apply { networkInterceptor?.let { addInterceptor(it) } }
            .pingInterval(socketSettings.resendPingIntervalMillis, TimeUnit.MILLISECONDS)
            .connectTimeout(socketSettings.connectTimeoutMillis, TimeUnit.MILLISECONDS)
            .readTimeout(socketSettings.readTimeoutMillis, TimeUnit.MILLISECONDS)
            .writeTimeout(socketSettings.writeTimeoutMillis, TimeUnit.MILLISECONDS)
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
        httpClientBuilder.addInterceptor(NetworkLoggerInterceptor())
        client = httpClientBuilder.build()
        request = Request.Builder()
            .url(threadsGateUrl)
            .build()
        listener = WebSocketListener()
    }

    override fun sendRatingDone(survey: Survey) {
        if (!survey.questions.isNullOrEmpty()) {
            val content = outgoingMessageCreator.createRatingDoneMessage(survey)
            val firstPartOfCorrelationId = "${ChatItemType.SURVEY_QUESTION_ANSWER.name}$CORRELATION_ID_DIVIDER"
            val secondPartOfCorrelationId = "${survey.sendingId}$CORRELATION_ID_DIVIDER${survey.questions?.first()?.correlationId}"
            val correlationId = "$firstPartOfCorrelationId$secondPartOfCorrelationId"
            surveysInProcess[survey.sendingId] = survey
            sendMessage(content, true, correlationId)
        }
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

    override fun sendRegisterDevice(forceRegistration: Boolean) {
        val deviceAddress = preferences.get<String>(PreferencesCoreKeys.DEVICE_ADDRESS)
        if (!deviceAddress.isNullOrBlank() || forceRegistration) {
            if (deviceAddress.isNullOrBlank()) sendRegisterDevice()
        } else {
            openWebSocket()
        }
    }

    override fun sendInitMessages() {
        sendInitChatMessage(true)
        sendEnvironmentMessage(true)
    }

    /**
     *  Метод для переотправки параметров устройства после изменения пуш токена
     */
    override fun updatePushToken() {
        sendRegisterDevice()
    }

    override fun sendMessage(
        userPhrase: UserPhrase,
        consultInfo: ConsultInfo?,
        filePath: String?,
        quoteFilePath: String?
    ): Boolean {
        userPhrase.campaignMessage?.let {
            campaignsInProcess[userPhrase.id] = it
        }
        val content = outgoingMessageCreator.createUserPhraseMessage(
            userPhrase,
            consultInfo,
            quoteFilePath,
            filePath
        )
        return sendMessage(
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
        val deviceAddress = preferences.get<String>(PreferencesCoreKeys.DEVICE_ADDRESS)
        if (deviceAddress.isNullOrEmpty()) {
            location = LatLng(latitude, longitude)
        } else {
            val content = outgoingMessageCreator.createMessageUpdateLocation(
                latitude,
                longitude,
                deviceInfo.getLocale(BaseConfig.instance.context)
            )
            sendMessage(content, sendInit = false)
        }
    }

    override fun getToken(): String {
        val userInfo = clientUseCase.getUserInfo()
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
        sendInit: Boolean = false
    ): Boolean {
        synchronized(messageInProcessIds) {
            messageInProcessIds.add(correlationId)
        }
        if (webSocket == null && tryOpeningWebSocket) {
            openWebSocket()
        }
        webSocket ?: return false
        val clientId = clientUseCase.getUserInfo()?.clientId
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

        try {
            if (content["type"].toString().contains(ChatItemType.INIT_CHAT.name)) {
                chatState.initChatCorrelationId = correlationId
            }
        } catch (ignored: NullPointerException) {}

        return sendMessageWithWebsocket(text)
    }

    private fun sendMessageWithWebsocket(message: String?): Boolean {
        var isSent = false
        if (message != null) {
            isSent = webSocket?.send(message) ?: false
            LoggerEdna.info(
                "[WS] ☛ Sending message with WS. Is sent: $isSent. Message: ${jsonFormatter.jsonToPrettyFormat(message)}"
            )
        }

        return isSent
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
        chatState.changeState(ChatStateEnum.REGISTERING_DEVICE)

        val clientId = clientUseCase.getUserInfo()?.clientId
        val deviceModel = getSimpleDeviceName()
        val deviceName = getDeviceName()
        val deviceAddress = preferences.get<String>(PreferencesCoreKeys.DEVICE_ADDRESS)
        val data = RegisterDeviceRequest.Data(
            appInfo.appId,
            appInfo.appVersion,
            threadsGateProviderUid,
            getCloudToken(),
            getDeviceUid(),
            "Android",
            deviceInfo.osVersion,
            deviceInfo.getLocale(BaseConfig.instance.context),
            Calendar.getInstance().timeZone.displayName,
            if (!TextUtils.isEmpty(deviceName)) deviceName else deviceModel,
            deviceModel,
            deviceAddress,
            clientId
        )
        val text = BaseConfig.instance.gson.toJson(
            RegisterDeviceRequest(UUID.randomUUID().toString(), data)
        )
        sendMessageWithWebsocket(text)
    }

    private fun getCloudToken(): String? {
        val fcmToken = preferences.get<String>(PreferencesCoreKeys.FCM_TOKEN)
        val hcmToken = preferences.get<String>(PreferencesCoreKeys.HCM_TOKEN)
        return fcmToken ?: hcmToken
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

    private fun sendInitChatMessage(tryOpeningWebSocket: Boolean): Boolean {
        chatState.changeState(ChatStateEnum.SENDING_INIT_USER)
        return sendMessage(
            content = outgoingMessageCreator.createInitChatMessage(),
            tryOpeningWebSocket = tryOpeningWebSocket,
            sendInit = false
        )
    }

    private fun sendEnvironmentMessage(tryOpeningWebSocket: Boolean): Boolean {
        return sendMessage(
            outgoingMessageCreator.createEnvironmentMessage(
                deviceInfo.getLocale(BaseConfig.instance.context)
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
            lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED)?.not() != false &&
            chatState.getCurrentState() >= ChatStateEnum.INIT_USER_SENT
        ) {
            closeWebSocket()
        }
    }

    @Synchronized
    override fun closeWebSocket() {
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
        } else {
            null
        }

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

    private fun saveInitUserId() {
        clientUseCase.getUserInfo()?.clientId?.let { userId ->
            preferences.save(PreferencesCoreKeys.INIT_SENT_LAST_USER_ID, userId, true)
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
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            LoggerEdna.info("[WS] ☚ ${jsonFormatter.jsonToPrettyFormat(text)}")
            postSocketResponseMap(text)
            val response = BaseConfig.instance.gson.fromJson(text, BaseResponse::class.java)
            val action = response.action
            val correlationId = response.correlationId
            if (response.data != null && response.data.has(KEY_ERROR)) {
                var errorMessage = response.data[KEY_ERROR].asString
                if (response.data.has(KEY_ERROR_DETAILS)) {
                    val errorDetails = response.data[KEY_ERROR_DETAILS].asString
                    if (!errorDetails.isNullOrEmpty()) {
                        errorMessage = "$errorMessage: $errorDetails"
                    }
                }
                chatUpdateProcessor.postError(TransportException(errorMessage))
            } else if (action != null) {
                if (action == Action.REGISTER_DEVICE && chatState.getCurrentState() > ChatStateEnum.LOGGED_OUT) {
                    val data = BaseConfig.instance.gson.fromJson(
                        response.data.toString(),
                        RegisterDeviceData::class.java
                    )
                    preferences.save(PreferencesCoreKeys.DEVICE_ADDRESS, data.deviceAddress)
                    data.deviceAddress?.let { chatUpdateProcessor.postDeviceAddressChanged(data.deviceAddress) }
                    location?.let { updateLocation(it.latitude, it.longitude) }
                    chatState.changeState(ChatStateEnum.DEVICE_REGISTERED)
                }
                if (action == Action.SEND_MESSAGE) {
                    val data = BaseConfig.instance.gson.fromJson(
                        response.data.toString(),
                        SendMessageData::class.java
                    )
                    val tokens = response.correlationId?.split(CORRELATION_ID_DIVIDER)?.toTypedArray()
                    if (tokens != null && tokens.size > 1) {
                        when (ChatItemType.fromString(tokens[0])) {
                            ChatItemType.MESSAGE -> {
                                if (campaignsInProcess.containsKey(tokens[1])) {
                                    campaignsInProcess[tokens[1]]?.let { campaignMessage ->
                                        chatUpdateProcessor.postCampaignMessageReplySuccess(campaignMessage)
                                    }
                                    campaignsInProcess.remove(tokens[1])
                                }
                                chatUpdateProcessor.postChatItemSendSuccess(
                                    ChatItemProviderData(
                                        tokens[1],
                                        data.messageId,
                                        data.sentAt?.time ?: Calendar.getInstance().time.time
                                    )
                                )
                            }
                            ChatItemType.SURVEY_QUESTION_ANSWER -> {
                                val sendingId = tokens[1].toLong()
                                val questionSendingId = tokens[2]
                                if (surveysInProcess.containsKey(sendingId)) {
                                    val survey = surveysInProcess[sendingId]
                                    survey?.copy()?.apply {
                                        questions?.removeAll { it.correlationId != questionSendingId }
                                    }?.let { copyToSend ->
                                        chatUpdateProcessor.postSurveySendSuccess(copyToSend)
                                    }
                                    survey?.questions?.toMutableList()?.let { questions ->
                                        questions.removeAll { it.correlationId == questionSendingId }
                                        if (questions.isEmpty()) {
                                            surveysInProcess.remove(sendingId)
                                        } else {
                                            surveysInProcess[sendingId]?.questions = ArrayList(questions)
                                        }
                                    } ?: surveysInProcess.remove(sendingId)
                                }
                            }
                            ChatItemType.REOPEN_THREAD, ChatItemType.CLOSE_THREAD ->
                                chatUpdateProcessor
                                    .postRemoveChatItem(ChatItemType.REQUEST_CLOSE_THREAD)
                            else -> {}
                        }
                    }
                    if (correlationId == chatState.initChatCorrelationId) {
                        val status = MessageStatus.fromString(data.status) ?: MessageStatus.SENDING
                        if (status >= MessageStatus.SENT && chatState.getCurrentState() < ChatStateEnum.INIT_USER_SENT) {
                            chatState.changeState(ChatStateEnum.INIT_USER_SENT)
                        }
                    }
                    chatUpdateProcessor.postOutgoingMessageStatusChanged(
                        listOf(
                            Status(
                                correlationId,
                                data.messageId,
                                MessageStatus.fromString(data.status) ?: MessageStatus.SENT
                            )
                        )
                    )
                }
                if (action == Action.GET_STATUSES) {
                    val data = BaseConfig.instance.gson.fromJson(
                        response.data.toString(),
                        GetStatusesData::class.java
                    )
                    data.statuses?.let { chatUpdateProcessor.postOutgoingMessageStatusChanged(it) }
                }
                if (action == Action.GET_MESSAGES) {
                    val data = BaseConfig.instance.gson.fromJson(
                        response.data.toString(),
                        GetMessagesData::class.java
                    )
                    val gson = BaseConfig.instance.gson
                    val messages = data.messages ?: listOf()
                    for (message in messages) {
                        if (message.content != null && message.content.has(MessageAttributes.TYPE)) {
                            val type = ChatItemType.fromString(messageParser.getType(message))
                            if (ChatItemType.TYPING == type) {
                                val content = gson.fromJson(
                                    message.content,
                                    TypingContent::class.java
                                )
                                content.clientId?.let { chatUpdateProcessor.postTyping(content.clientId) }
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
                                val chatItem = messageParser.format(message)
                                if (chatItem is SpeechMessageUpdate) {
                                    chatUpdateProcessor.postSpeechMessageUpdate(chatItem)
                                }
                            } else {
                                val chatItem = messageParser.format(message)
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
            LoggerEdna.info("[WS] ☚ Receiving bytes: ${bytes.hex()}")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            postSocketResponseMap(code, reason)
            LoggerEdna.info("[WS] ☚ Websocket closing: $code / $reason")
            webSocket.close(NORMAL_CLOSURE_STATUS, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            postSocketResponseMap(code, reason)
            preferences.save("", PreferencesCoreKeys.DEVICE_ADDRESS)
            this@ThreadsGateTransport.webSocket = null
            LoggerEdna.info("[WS] ☚ Websocket closed: $code / $reason")
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
            LoggerEdna.info("[WS] ☚\u274C On Websocket error : ${t.message}")
            val message = if (t.localizedMessage.isNullOrBlank()) t.message else t.localizedMessage
            chatUpdateProcessor.postError(TransportException(message))
            synchronized(messageInProcessIds) {
                coroutineScope.launch {
                    for (i in 0 until messageInProcessIds.size) {
                        val messageId = messageInProcessIds[i]
                        val result = coroutineScope.async(Dispatchers.IO) {
                            database.getChatItemByCorrelationId(messageId)
                        }
                        val dbItem = result.await()
                        val messageStatus = getMessageStatus(dbItem, Status(messageId, null, MessageStatus.FAILED))
                        if (messageStatus.status == MessageStatus.FAILED) {
                            LoggerEdna.info("Starting process error for messageId: $messageId")
                            processMessageSendError(messageId)
                        }
                    }
                    messageInProcessIds.clear()
                    closeWebSocket()
                }
            }
        }

        private fun getMessageStatus(chatItem: ChatItem?, receivedStatus: Status): Status {
            val item = (chatItem as? UserPhrase)
            return if (item != null) {
                if (item.sentState.ordinal > receivedStatus.status.ordinal) {
                    Status(
                        item.backendMessageId ?: receivedStatus.correlationId,
                        item.backendMessageId ?: receivedStatus.messageId,
                        item.sentState
                    )
                } else {
                    receivedStatus
                }
            } else {
                receivedStatus
            }
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
        private const val KEY_ERROR_DETAILS = "errorDetails"
        private const val KEY_CODE = "code"
        private const val KEY_REASON = "reason"
        private const val KEY_PROTOCOL = "protocol"
        private const val KEY_MESSAGE = "message"
        private const val KEY_URL = "url"
    }
}
