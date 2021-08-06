package im.threads.push;

import com.edna.android.push_lite.fcm.FcmPushService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import androidx.annotation.NonNull;
import im.threads.ConfigBuilder;
import im.threads.internal.Config;
import im.threads.internal.services.NotificationService;
import im.threads.internal.transport.MessageAttributes;
import im.threads.internal.transport.mfms_push.MFMSPushMessageParser;
import im.threads.internal.transport.mfms_push.ShortPushMessageProcessingDelegate;
import im.threads.internal.utils.PrefUtils;

public class ThreadsPushFcmIntentService extends FcmPushService {

    private static final String MESSAGE_ID = "messageId";
    private static final String SERVER_MESSAGE_ID = "serverMessageId";
    private static final String CHL_SENT_AT = "chlSentAt";

    @Override
    public void onNewToken(@NonNull String token) {
        if (Config.instance.transport.getType() == ConfigBuilder.TransportType.MFMS_PUSH) {
            super.onNewToken(token);
        }
        PrefUtils.setFcmToken(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);
        Map<String, String> data = message.getData();
        if (Config.instance.transport.getType() == ConfigBuilder.TransportType.MFMS_PUSH) {
            if (data.containsKey(CHL_SENT_AT) && (!data.containsKey(MESSAGE_ID) || !data.containsKey(SERVER_MESSAGE_ID))) {
                ShortPushMessageProcessingDelegate.INSTANCE.process(this, MFMSPushMessageParser.mapToBundle(data), null);
            }
        } else if (Config.instance.transport.getType() == ConfigBuilder.TransportType.THREADS_GATE) {
            if (MessageAttributes.THREADS.equals(data.get(MessageAttributes.ORIGIN))) {
                String operatorUrl = data.get(MessageAttributes.OPERATOR_URL);
                String appMarker = data.get(MessageAttributes.APP_MARKER_KEY);
                String text = data.get(MessageAttributes.MESSAGE);
                NotificationService.addUnreadMessage(this, text, operatorUrl, appMarker);
            }
        }
    }
}
