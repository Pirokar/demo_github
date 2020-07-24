package im.threads.internal.transport.threads_gate;

import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;

import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import im.threads.internal.Config;
import im.threads.internal.chat_updates.ChatUpdateProcessor;
import im.threads.internal.transport.OutgoingMessageCreator;
import im.threads.internal.transport.TransportException;
import im.threads.internal.transport.threads_gate.requests.RegisterDeviceRequest;
import im.threads.internal.transport.threads_gate.requests.SendMessageRequest;
import im.threads.internal.transport.threads_gate.responses.BaseResponse;
import im.threads.internal.transport.threads_gate.responses.RegisterDeviceData;
import im.threads.internal.utils.AppInfoHelper;
import im.threads.internal.utils.DeviceInfoHelper;
import im.threads.internal.utils.PrefUtils;
import im.threads.internal.utils.ThreadsLogger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okio.ByteString;

public class ClientOfflineSender {

    private static final String TAG = ClientOfflineSender.class.getCanonicalName();

    private final OkHttpClient client;
    private final Request request;
    private final String threadsGateProviderUid;
    @Nullable
    private WebSocket webSocket;

    public ClientOfflineSender(String threadsGateUrl, String threadsGateProviderUid) {
        this.client = new OkHttpClient.Builder()
                .pingInterval(10_000, TimeUnit.MILLISECONDS)
                .build();
        this.request = new Request.Builder()
                .url(threadsGateUrl)
                .build();
        this.threadsGateProviderUid = threadsGateProviderUid;
    }

    public void sendClientOffline(String clientId) {
        if (webSocket == null) {
            webSocket = client.newWebSocket(request, new ClientOfflineSocketListener(clientId));
        }
        sendRegisterDevice();
    }

    private void sendRegisterDevice() {
        if (webSocket == null) {
            return;
        }
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
        String text = Config.instance.gson.toJson(
                new RegisterDeviceRequest(UUID.randomUUID().toString(), data)
        );
        ThreadsLogger.i(TAG, "Sending : " + text);
        webSocket.send(text);
    }

    private final class ClientOfflineSocketListener extends okhttp3.WebSocketListener {

        private static final int NORMAL_CLOSURE_STATUS = 1000;

        private final String clientId;

        ClientOfflineSocketListener(String clientId) {
            this.clientId = clientId;
        }

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
                    final JsonObject content = OutgoingMessageCreator.createMessageClientOffline(clientId);
                    content.addProperty("deviceAddress", PrefUtils.getDeviceAddress());
                    if (webSocket == null) {
                        return;
                    }
                    String request = Config.instance.gson.toJson(
                            new SendMessageRequest(
                                    UUID.randomUUID().toString(),
                                    new SendMessageRequest.Data(PrefUtils.getDeviceAddress(), content, false)
                            )
                    );
                    ThreadsLogger.i(TAG, "Sending : " + request);
                    webSocket.send(request);
                    return;
                }
            }
            closeWebSocket();
        }

        private synchronized void closeWebSocket() {
            if (webSocket != null) {
                webSocket.close(NORMAL_CLOSURE_STATUS, null);
                webSocket = null;
            }
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
        }
    }


}
