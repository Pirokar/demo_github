package im.threads.push;

import com.google.firebase.messaging.RemoteMessage;
import com.mfms.android.push_lite.PushFcmIntentService;

import im.threads.ConfigBuilder;
import im.threads.internal.Config;
import im.threads.internal.services.NotificationService;
import im.threads.internal.transport.MessageAttributes;
import im.threads.internal.utils.PrefUtils;

public class ThreadsPushFcmIntentService extends PushFcmIntentService {

    @Override
    public void onNewToken(String token) {
        if (Config.instance.transport.getType() == ConfigBuilder.TransportType.MFMS_PUSH) {
            super.onNewToken(token);
        }
        PrefUtils.setFcmToken(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        if (Config.instance.transport.getType() == ConfigBuilder.TransportType.MFMS_PUSH) {
            super.onMessageReceived(message);
        } else if (Config.instance.transport.getType() == ConfigBuilder.TransportType.THREADS_GATE) {
            if (MessageAttributes.THREADS.equals(message.getData().get(MessageAttributes.ORIGIN))) {
                String operatorUrl = message.getData().get(MessageAttributes.OPERATOR_URL);
                String appMarker = message.getData().get(MessageAttributes.APP_MARKER_KEY);
                String text = message.getData().get(MessageAttributes.MESSAGE);
                NotificationService.addUnreadMessage(this, text, operatorUrl, appMarker);
            }
        }
    }
}