package im.threads.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.mfms.android.push_lite.PushBroadcastReceiver;

import java.util.List;

import im.threads.internal.broadcastReceivers.ProgressReceiver;
import im.threads.internal.chat_updates.ChatUpdateProcessor;
import im.threads.internal.formatters.IncomingMessageParser;
import im.threads.internal.formatters.PushMessageAttributes;
import im.threads.internal.formatters.PushMessageType;
import im.threads.internal.services.NotificationService;
import im.threads.internal.utils.ThreadsLogger;

/**
 * Приемщик всех коротких пуш уведомлений,
 * т.е. просто всех уведомлений напрямую от GCM.
 * Полная информация о пуш уведомлениях скачивается отдельно
 * и доступна в {@link ThreadsPushServerIntentService}
 */
public class ThreadsPushBroadcastReceiver extends PushBroadcastReceiver {
    private static final String TAG = "ThreadsPushBroadcastReceiver ";

    @Override
    protected void onNewPushNotification(final Context context, final String alert, final Bundle bundle) {
        ThreadsLogger.i(TAG, "onNewPushNotification " + alert + " " + bundle);
        if (IncomingMessageParser.isThreadsOriginPush(bundle)) {
            final PushMessageType pushMessageType = PushMessageType.getKnownType(bundle);
            switch (pushMessageType) {
                case TYPING:
                    String clientId = bundle.getString(PushMessageAttributes.CLIENT_ID);
                    if (clientId != null) {
                        ChatUpdateProcessor.getInstance().postTyping(clientId);
                    }
                    break;
                case MESSAGES_READ:
                    final List<String> list = IncomingMessageParser.getReadIds(bundle);
                    ThreadsLogger.i(TAG, "onSystemMessageFromServer: read messages " + list);
                    for (final String readMessageProviderId : list) {
                        ChatUpdateProcessor.getInstance().postMessageRead(readMessageProviderId);
                    }
                    break;
                case REMOVE_PUSHES:
                    NotificationService.removeNotification(context);
                    break;
                case UNREAD_MESSAGE_NOTIFICATION:
                    String operatorUrl = bundle.getString(PushMessageAttributes.OPERATOR_URL);
                    String appMarker = bundle.getString(PushMessageAttributes.APP_MARKER_KEY);
                    NotificationService.addUnreadMessage(context, alert, operatorUrl, appMarker);
                    break;
            }
        }
    }

    @Override
    public void onStatusChanged(final Context context, final String s) {
        ThreadsLogger.e(TAG, "onStatusChanged " + s);
    }

    @Override
    public void onDeviceAddressChanged(final Context context, final String s) {
        ThreadsLogger.i(TAG, "onDeviceAddressChanged " + s);
        new Thread(() -> context.sendBroadcast(new Intent(ProgressReceiver.DEVICE_ID_IS_SET_BROADCAST)))
                .start();
    }

    @Override
    public void onDeviceAddressProblems(final Context context, final String s) {
        ThreadsLogger.w(TAG, "onDeviceAddressProblems " + s);
    }

    @Override
    public void onError(final Context context, final String s) {
        ThreadsLogger.e(TAG, "onFileDonwloaderError " + s);
    }
}
