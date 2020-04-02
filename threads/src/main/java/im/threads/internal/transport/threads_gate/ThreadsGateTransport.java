package im.threads.internal.transport.threads_gate;

import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.ObjectsCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import im.threads.ConfigBuilder;
import im.threads.internal.Config;
import im.threads.internal.chat_updates.ChatUpdateProcessor;
import im.threads.internal.formatters.ChatItemType;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ConsultInfo;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.transport.ChatItemProviderData;
import im.threads.internal.transport.MessageAttributes;
import im.threads.internal.transport.OutgoingMessageCreator;
import im.threads.internal.transport.Transport;
import im.threads.internal.transport.TransportException;
import im.threads.internal.transport.models.TypingContent;
import im.threads.internal.transport.threads_gate.requests.RegisterDeviceRequest;
import im.threads.internal.transport.threads_gate.requests.SendMessageRequest;
import im.threads.internal.transport.threads_gate.requests.UpdateStatusesRequest;
import im.threads.internal.transport.threads_gate.responses.BaseMessage;
import im.threads.internal.transport.threads_gate.responses.BaseResponse;
import im.threads.internal.transport.threads_gate.responses.GetMessagesData;
import im.threads.internal.transport.threads_gate.responses.GetStatusesData;
import im.threads.internal.transport.threads_gate.responses.RegisterDeviceData;
import im.threads.internal.transport.threads_gate.responses.SendMessageData;
import im.threads.internal.transport.threads_gate.responses.UpdateStatusesData;
import im.threads.internal.utils.AppInfoHelper;
import im.threads.internal.utils.DeviceInfoHelper;
import im.threads.internal.utils.PrefUtils;
import im.threads.internal.utils.ThreadsLogger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.ByteString;

public class ThreadsGateTransport implements Transport, LifecycleObserver {

    private static final String TAG = ThreadsGateTransport.class.getSimpleName();
    private static final String CORRELATION_ID_DIVIDER = ":";

    private final OkHttpClient client;
    private final WebSocketListener listener;
    private final Request request;
    private final String threadsGateProviderUid;
    @Nullable
    private WebSocket webSocket;
    @Nullable
    private Lifecycle lifecycle;

    private final List<String> messageInProcessIds = new ArrayList<>();

    public ThreadsGateTransport(String threadsGateUrl, String threadsGateProviderUid, boolean isDebugLoggingEnabled) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .pingInterval(10_000, TimeUnit.MILLISECONDS);
        if (isDebugLoggingEnabled) {
            clientBuilder.addInterceptor(new HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY));
        }
        this.client = clientBuilder.build();
        this.listener = new WebSocketListener();
        this.request = new Request.Builder()
                .url(threadsGateUrl)
                .build();
        this.threadsGateProviderUid = threadsGateProviderUid;
    }

    @Override
    public void init() {
    }

    @Override
    public void sendRatingDone(Survey survey) {
        final JsonObject content = OutgoingMessageCreator.createRatingDoneMessage(
                survey,
                PrefUtils.getClientID(),
                PrefUtils.getAppMarker()
        );
        sendMessage(content, true, ChatItemType.SURVEY_QUESTION_ANSWER.name() + CORRELATION_ID_DIVIDER + survey.getSendingId());
    }

    @Override
    public void sendResolveThread(boolean approveResolve) {
        final String clientID = PrefUtils.getClientID();
        final JsonObject content = approveResolve ?
                OutgoingMessageCreator.createResolveThreadMessage(clientID) :
                OutgoingMessageCreator.createReopenThreadMessage(clientID);
        sendMessage(content, false, approveResolve ? ChatItemType.CLOSE_THREAD : ChatItemType.REOPEN_THREAD);
    }

    @Override
    public void sendUserTying(String input) {
        final JsonObject content = OutgoingMessageCreator.createMessageTyping(PrefUtils.getClientID(), input);
        sendMessage(content, false);
    }

    @Override
    public void sendInitChatMessage() {
        final JsonObject content = OutgoingMessageCreator.createInitChatMessage(PrefUtils.getClientID(), PrefUtils.getData());
        sendMessage(content, false);
    }

    @Override
    public void sendEnvironmentMessage() {
        final JsonObject content = OutgoingMessageCreator.createEnvironmentMessage(
                PrefUtils.getUserName(),
                PrefUtils.getClientID(),
                PrefUtils.getClientIDEncrypted(),
                PrefUtils.getData(),
                Config.instance.context
        );
        sendMessage(content, false);
    }

    @Override
    public void sendMessageRead(String messageId) {
        if (webSocket == null) {
            openWebSocket();
            if (webSocket == null) {
                return;
            }
        }
        ArrayList<UpdateStatusesRequest.MessageStatusData> messageIds = new ArrayList<>();
        messageIds.add(new UpdateStatusesRequest.MessageStatusData(messageId, MessageStatus.READ));
        webSocket.send(
                Config.instance.gson.toJson(
                        new UpdateStatusesRequest(
                                UUID.randomUUID().toString(),
                                new UpdateStatusesRequest.Data(PrefUtils.getDeviceAddress(), messageIds)
                        )
                )
        );
    }

    @Override
    public void sendMessage(UserPhrase userPhrase, ConsultInfo consultInfo, String filePath, String quoteFilePath) {
        final JsonObject content = OutgoingMessageCreator.createUserPhraseMessage(
                userPhrase,
                consultInfo,
                quoteFilePath,
                filePath,
                PrefUtils.getClientID(),
                PrefUtils.getThreadID()
        );
        sendMessage(content, true, ChatItemType.MESSAGE.name() + CORRELATION_ID_DIVIDER + userPhrase.getUuid());
    }

    @Override
    public void sendRatingReceived(long sendingId) {
        final JsonObject content = OutgoingMessageCreator.createRatingReceivedMessage(
                sendingId,
                PrefUtils.getClientID()
        );
        sendMessage(content, false);
    }

    @Override
    public void sendClientOffline(String clientId) {
        final JsonObject content = OutgoingMessageCreator.createMessageClientOffline(
                PrefUtils.getClientID()
        );
        sendMessage(content, false);
    }

    @Override
    public ConfigBuilder.TransportType getType() {
        return ConfigBuilder.TransportType.THREADS_GATE;
    }

    @NonNull
    @Override
    public String getToken() {
        String clientIdSignature = PrefUtils.getClientIdSignature();
        return (TextUtils.isEmpty(clientIdSignature) ? PrefUtils.getDeviceAddress() : clientIdSignature)
                + ":" + PrefUtils.getClientID();
    }

    @Override
    public synchronized void setLifecycle(@NonNull Lifecycle lifecycle) {
        if (this.lifecycle != null) {
            this.lifecycle.removeObserver(this);
        }
        this.lifecycle = lifecycle;
        this.lifecycle.addObserver(this);
    }

    private void sendMessage(JsonObject content, boolean important) {
        sendMessage(content, important, UUID.randomUUID().toString());
    }

    private void sendMessage(JsonObject content, boolean important, ChatItemType type) {
        sendMessage(content, important, type.name() + CORRELATION_ID_DIVIDER + UUID.randomUUID().toString());
    }

    private void sendMessage(JsonObject content, boolean important, String correlationId) {
        synchronized (messageInProcessIds) {
            messageInProcessIds.add(correlationId);
        }
        if (webSocket == null) {
            openWebSocket();
        }
        if (webSocket == null) {
            return;
        }
        webSocket.send(
                Config.instance.gson.toJson(
                        new SendMessageRequest(
                                correlationId,
                                new SendMessageRequest.Data(PrefUtils.getDeviceAddress(), content, important)
                        )
                )
        );
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private synchronized void openWebSocket() {
        if (webSocket == null) {
            webSocket = client.newWebSocket(request, listener);
            String deviceModel = Build.MANUFACTURER + ' ' + Build.MODEL;
            String deviceName = Settings.Secure.getString(Config.instance.context.getContentResolver(), "bluetooth_name");
            RegisterDeviceRequest.Data data = new RegisterDeviceRequest.Data(
                    AppInfoHelper.getAppId(),
                    AppInfoHelper.getAppVersion(),
                    threadsGateProviderUid,
                    PrefUtils.getFcmToken(),
                    PrefUtils.getDeviceUid(),
                    "Android",
                    DeviceInfoHelper.getOsVersion(),
                    DeviceInfoHelper.getLocale(Config.instance.context),
                    Calendar.getInstance().getTimeZone().getDisplayName(),
                    !TextUtils.isEmpty(deviceName) ? deviceName : deviceModel,
                    deviceModel,
                    PrefUtils.getDeviceAddress()
            );
            webSocket.send(
                    Config.instance.gson.toJson(
                            new RegisterDeviceRequest(UUID.randomUUID().toString(), data)
                    )
            );
        }
    }

    /**
     * Closes websocket connection if there are no messages left to send and user is not interacting with chat screen
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void closeWebSocketIfNeeded() {
        if (messageInProcessIds.isEmpty() && (lifecycle == null || !lifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED))) {
            closeWebSocket();
        }
    }

    private synchronized void closeWebSocket() {
        if (webSocket != null) {
            webSocket.close(WebSocketListener.NORMAL_CLOSURE_STATUS, null);
            webSocket = null;
        }
    }

    private void processMessageSendError(String correlationId) {
        String[] tokens = correlationId.split(CORRELATION_ID_DIVIDER);
        if (tokens.length > 1) {
            ChatItemType type = ChatItemType.fromString(tokens[0]);
            if (type == ChatItemType.MESSAGE) {
                ChatUpdateProcessor.getInstance().postChatItemSendError(tokens[1]);
            }
        }
    }

    private final class WebSocketListener extends okhttp3.WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            ThreadsLogger.i(TAG, "OnOpen : " + response);
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            ThreadsLogger.i(TAG, "Receiving : " + text);
            BaseResponse response = Config.instance.gson.fromJson(text, BaseResponse.class);
            Action action = response.getAction();
            if (response.getData().has("error")) {
                ChatUpdateProcessor.getInstance().postError(new TransportException(response.getData().get("error").getAsString()));
            } else if (action != null) {
                if (action.equals(Action.REGISTER_DEVICE)) {
                    RegisterDeviceData data = Config.instance.gson.fromJson(response.getData().toString(), RegisterDeviceData.class);
                    PrefUtils.setDeviceAddress(data.getDeviceAddress());
                    sendInitChatMessage();
                    sendEnvironmentMessage();
                }
                if (action.equals(Action.SEND_MESSAGE)) {
                    SendMessageData data = Config.instance.gson.fromJson(response.getData().toString(), SendMessageData.class);
                    String[] tokens = response.getCorrelationId().split(CORRELATION_ID_DIVIDER);
                    if (tokens.length > 1) {
                        ChatItemType type = ChatItemType.fromString(tokens[0]);
                        switch (type) {
                            case MESSAGE:
                                ChatUpdateProcessor.getInstance().postChatItemSendSuccess(new ChatItemProviderData(tokens[1], data.getMessageId(), data.getSentAt().getTime()));
                                break;
                            case SURVEY_QUESTION_ANSWER:
                                ChatUpdateProcessor.getInstance().postSurveySendSuccess(Long.parseLong(tokens[1]));
                                break;
                            case REOPEN_THREAD:
                            case CLOSE_THREAD:
                                ChatUpdateProcessor.getInstance().postRemoveChatItem(ChatItemType.REQUEST_CLOSE_THREAD);
                                break;
                        }
                    }
                }
                if (action.equals(Action.GET_STATUSES)) {
                    GetStatusesData data = Config.instance.gson.fromJson(response.getData().toString(), GetStatusesData.class);
                    for (final GetStatusesData.Status status : data.getStatuses()) {
                        if (ObjectsCompat.equals(MessageStatus.READ, status.getStatus())) {
                            ChatUpdateProcessor.getInstance().postUserMessageWasRead(status.getMessageId());
                        }
                    }
                }
                if (action.equals(Action.UPDATE_STATUSES)) {
                    UpdateStatusesData data = Config.instance.gson.fromJson(response.getData().toString(), UpdateStatusesData.class);
                    for (final String messageId : data.getMessageIds()) {
                        ChatUpdateProcessor.getInstance().postConsultMessageWasRead(messageId);
                    }
                }
                if (action.equals(Action.GET_MESSAGES)) {
                    GetMessagesData data = Config.instance.gson.fromJson(response.getData().toString(), GetMessagesData.class);
                    for (BaseMessage message : data.getMessages()) {
                        if (message.getContent().has(MessageAttributes.TYPE)) {
                            final ChatItemType type = ChatItemType.fromString(ThreadsGateMessageParser.getType(message));
                            if (ChatItemType.TYPING.equals(type)) {
                                TypingContent content = Config.instance.gson.fromJson(message.getContent(), TypingContent.class);
                                if (content.getClientId() != null) {
                                    ChatUpdateProcessor.getInstance().postTyping(content.getClientId());
                                }
                            } else if (ThreadsGateMessageParser.checkId(message, PrefUtils.getClientID())) {
                                ChatItem chatItem = ThreadsGateMessageParser.format(message);
                                if (chatItem != null) {
                                    ChatUpdateProcessor.getInstance().postNewMessage(chatItem);
                                }
                            }
                        }
                    }
                }
            }
            synchronized (messageInProcessIds) {
                messageInProcessIds.remove(response.getCorrelationId());
            }
            closeWebSocketIfNeeded();
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            ThreadsLogger.i(TAG, "Receiving bytes : " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            ThreadsLogger.i(TAG, "Closing : " + code + " / " + reason);
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            ThreadsLogger.i(TAG, "Error : " + t.getMessage());
            ChatUpdateProcessor.getInstance().postError(new TransportException(t.getMessage()));
            synchronized (messageInProcessIds) {
                for (String messageId : messageInProcessIds) {
                    processMessageSendError(messageId);
                }
                messageInProcessIds.clear();
            }
            closeWebSocket();
        }
    }
}
