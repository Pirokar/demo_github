package im.threads.internal.transport.threads_gate

import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.core.util.ObjectsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.gson.JsonObject
import im.threads.ConfigBuilder.TransportType
import im.threads.internal.Config
import im.threads.internal.chat_updates.ChatUpdateProcessor
import im.threads.internal.formatters.ChatItemType
import im.threads.internal.model.ConsultInfo
import im.threads.internal.model.SpeechMessageUpdate
import im.threads.internal.model.Survey
import im.threads.internal.model.UserPhrase
import im.threads.internal.transport.ApplicationConfig
import im.threads.internal.transport.AuthInterceptor
import im.threads.internal.transport.ChatItemProviderData
import im.threads.internal.transport.MessageAttributes
import im.threads.internal.transport.OutgoingMessageCreator
import im.threads.internal.transport.Transport
import im.threads.internal.transport.TransportException
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
import im.threads.internal.utils.ThreadsLogger
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.logging.HttpLoggingInterceptor
import okio.ByteString
import java.util.ArrayList
import java.util.Calendar
import java.util.HashMap
import java.util.UUID
import java.util.concurrent.TimeUnit

class ThreadsGateTransport(
    threadsGateUrl: String,
    threadsGateProviderUid: String,
    threadsGateHuaweiProviderUid: String?,
    isDebugLoggingEnabled: Boolean
) : Transport(), LifecycleObserver {
    private val client: OkHttpClient
    private val request: Request
    private val listener: WebSocketListener
    private val applicationConfig: ApplicationConfig
    private val messageInProcessIds: MutableList<String> = ArrayList()
    private val surveysInProcess: MutableMap<Long, Survey> = HashMap()
    private var webSocket: WebSocket? = null
    private var lifecycle: Lifecycle? = null

    init {
        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .pingInterval(10000, TimeUnit.MILLISECONDS)
        if (isDebugLoggingEnabled) {
            clientBuilder.addInterceptor(
                HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY)
            )
        }
        client = clientBuilder.build()
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
            PrefUtils.getClientID(),
            PrefUtils.getAppMarker()
        )
        surveysInProcess[survey.sendingId] = survey
        sendMessage(
            content,
            true,
            ChatItemType.SURVEY_QUESTION_ANSWER.name + CORRELATION_ID_DIVIDER + survey.sendingId
        )
    }

    override fun sendResolveThread(approveResolve: Boolean) {
        val clientID = PrefUtils.getClientID()
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
        sendMessage(OutgoingMessageCreator.createMessageTyping(PrefUtils.getClientID(), input))
    }

    override fun sendInit() {
        if (!TextUtils.isEmpty(PrefUtils.getDeviceAddress())) {
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
        ThreadsLogger.i(
            TAG,
            "sendMessage: userPhrase = $userPhrase, consultInfo = $consultInfo, filePath = $filePath, quoteFilePath = $quoteFilePath"
        )
        val content = OutgoingMessageCreator.createUserPhraseMessage(
            userPhrase,
            consultInfo,
            quoteFilePath,
            filePath,
            PrefUtils.getClientID()
        )
        sendMessage(
            content,
            true,
            ChatItemType.MESSAGE.name + CORRELATION_ID_DIVIDER + userPhrase.id
        )
    }

    override fun sendRatingReceived(survey: Survey) {
        sendMessage(
            OutgoingMessageCreator.createRatingReceivedMessage(
                survey.sendingId,
                PrefUtils.getClientID()
            ),
            false,
            ChatItemType.SURVEY_PASSED.name + CORRELATION_ID_DIVIDER + survey.uuid
        )
    }

    override fun sendClientOffline(clientId: String) {
        if (TextUtils.isEmpty(PrefUtils.getDeviceAddress())) {
            return
        }
        val content = OutgoingMessageCreator.createMessageClientOffline(
            clientId
        )
        sendMessage(content, sendInit = false)
    }

    override fun getType(): TransportType {
        return TransportType.THREADS_GATE
    }

    override fun getToken(): String {
        val clientIdSignature = PrefUtils.getClientIdSignature()
        return ((if (TextUtils.isEmpty(clientIdSignature)) PrefUtils.getDeviceAddress() else clientIdSignature)
                + ":" + PrefUtils.getClientID())
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
        ThreadsLogger.i(
            TAG,
            "sendMessage: content = $content, important = $important, correlationId = $correlationId"
        )
        synchronized(messageInProcessIds) {
            messageInProcessIds.add(correlationId)
        }
        if (webSocket == null && tryOpeningWebSocket) {
            openWebSocket()
        }
        val ws = webSocket ?: return
        if (sendInit &&
            !TextUtils.isEmpty(PrefUtils.getClientID()) &&
            !TextUtils.isEmpty(PrefUtils.getDeviceAddress())
        ) {
            sendInitChatMessage(false)
            sendEnvironmentMessage(false)
        }
        val text = Config.instance.gson.toJson(
            SendMessageRequest(
                correlationId,
                SendMessageRequest.Data(PrefUtils.getDeviceAddress(), content, important)
            )
        )
        ThreadsLogger.i(TAG, "Sending : $text")
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
        val data = RegisterDeviceRequest.Data(
            AppInfoHelper.getAppId(),
            AppInfoHelper.getAppVersion(),
            applicationConfig.getProviderUid(),
            applicationConfig.getCloudMessagingToken(),
            PrefUtils.getDeviceUid(),
            "Android",
            DeviceInfoHelper.getOsVersion(),
            DeviceInfoHelper.getLocale(Config.instance.context),
            Calendar.getInstance().timeZone.displayName,
            if (!TextUtils.isEmpty(deviceName)) deviceName else deviceModel,
            deviceModel,
            PrefUtils.getDeviceAddress()
        )
        val text = Config.instance.gson.toJson(
            RegisterDeviceRequest(UUID.randomUUID().toString(), data)
        )
        ThreadsLogger.i(TAG, "Sending : $text")
        ws.send(text)
    }

    private fun sendInitChatMessage(tryOpeningWebSocket: Boolean) {
        sendMessage(
            content = OutgoingMessageCreator.createInitChatMessage(
                PrefUtils.getClientID(),
                PrefUtils.getData()
            ),
            tryOpeningWebSocket = tryOpeningWebSocket,
            sendInit = false
        )
    }

    private fun sendEnvironmentMessage(tryOpeningWebSocket: Boolean) {
        sendMessage(
            OutgoingMessageCreator.createEnvironmentMessage(
                PrefUtils.getUserName(),
                PrefUtils.getClientID(),
                PrefUtils.getClientIDEncrypted(),
                PrefUtils.getData(),
                Config.instance.context
            ),
            tryOpeningWebSocket = tryOpeningWebSocket,
            sendInit = false
        )
    }

    /**
     * Closes websocket connection if there are no messages left to send and user is not interacting with chat screen
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
                ChatUpdateProcessor.getInstance().postChatItemSendError(tokens[1])
            }
        }
    }

    private inner class WebSocketListener : okhttp3.WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            ThreadsLogger.i(TAG, "OnOpen : $response")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            ThreadsLogger.i(TAG, "Receiving : $text")
            val response = Config.instance.gson.fromJson(text, BaseResponse::class.java)
            val action = response.action
            if (response.data.has("error")) {
                ChatUpdateProcessor.getInstance()
                    .postError(TransportException(response.data["error"].asString))
            } else if (action != null) {
                if (action == Action.REGISTER_DEVICE) {
                    val data = Config.instance.gson.fromJson(
                        response.data.toString(),
                        RegisterDeviceData::class.java
                    )
                    val initialRegistration = TextUtils.isEmpty(PrefUtils.getDeviceAddress())
                    PrefUtils.setDeviceAddress(data.deviceAddress)
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
                            ChatItemType.MESSAGE -> ChatUpdateProcessor.getInstance()
                                .postChatItemSendSuccess(
                                    ChatItemProviderData(
                                        tokens[1], data.messageId, data.sentAt.time
                                    )
                                )
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
                /*if (action.equals(Action.UPDATE_STATUSES)) {
                    UpdateStatusesData data = Config.instance.gson.fromJson(response.getData().toString(), UpdateStatusesData.class);
                    for (final String messageId : data.getMessageIds()) {
                        ChatUpdateProcessor.getInstance().postMessageWasRead(messageId);
                    }
                }*/if (action == Action.GET_MESSAGES) {
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
                                if (content.clientId != null || Config.instance.clientIdIgnoreEnabled) {
                                    ChatUpdateProcessor.getInstance().postTyping(content.clientId)
                                }
                            } else if (ChatItemType.ATTACHMENT_SETTINGS == type) {
                                val attachmentSettings = Config.instance.gson.fromJson(
                                    message.content,
                                    AttachmentSettings::class.java
                                )
                                if (attachmentSettings.clientId != null || Config.instance.clientIdIgnoreEnabled) {
                                    ChatUpdateProcessor.getInstance()
                                        .postAttachmentSettings(attachmentSettings)
                                }
                            } else if (ChatItemType.SPEECH_MESSAGE_UPDATED == type) {
                                val chatItem = ThreadsGateMessageParser.format(message)
                                if (chatItem is SpeechMessageUpdate) {
                                    ChatUpdateProcessor.getInstance()
                                        .postSpeechMessageUpdate(chatItem)
                                }
                            } else if (ThreadsGateMessageParser.checkId(
                                    message,
                                    PrefUtils.getClientID()
                                )
                            ) {
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

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            ThreadsLogger.i(TAG, "Receiving bytes : " + bytes.hex())
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            ThreadsLogger.i(TAG, "Closing : $code / $reason")
            webSocket.close(Companion.NORMAL_CLOSURE_STATUS, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            ThreadsLogger.i(TAG, "OnClosed : $code / $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            ThreadsLogger.i(TAG, "Error : " + t.message)
            ChatUpdateProcessor.getInstance().postError(TransportException(t.message))
            synchronized(messageInProcessIds) {
                for (messageId in messageInProcessIds) {
                    processMessageSendError(messageId)
                }
                messageInProcessIds.clear()
            }
            closeWebSocket()
        }
    }

    companion object {
        private const val TAG = "ThreadsGateTransport"
        private const val CORRELATION_ID_DIVIDER = ":"
        const val NORMAL_CLOSURE_STATUS = 1000
    }
}
