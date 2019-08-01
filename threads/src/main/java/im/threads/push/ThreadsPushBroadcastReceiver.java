package im.threads.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.mfms.android.push_lite.PushBroadcastReceiver;

import im.threads.internal.broadcastReceivers.ProgressReceiver;
import im.threads.internal.controllers.ChatController;
import im.threads.internal.formatters.IncomingMessageParser;
import im.threads.internal.formatters.PushMessageTypes;
import im.threads.internal.utils.ThreadsLogger;

/**
 * Приемщик всех коротких пуш уведомлений,
 * т.е. просто всех уведомлений на прямую от GCM.
 * Полная информация о пуш уведомлениях скачивается отдельно
 * и доступна в {@link ThreadsPushServerIntentService}
 */
public class ThreadsPushBroadcastReceiver extends PushBroadcastReceiver {
    private static final String TAG = "ThreadsPushBroadcastReceiver ";

    @Override
    protected void onNewPushNotification(final Context context, final String alert, final Bundle bundle) {
        ThreadsLogger.i(TAG, "onNewPushNotification " + alert + " " + bundle);
        if (IncomingMessageParser.isThreadsOriginPush(bundle)) {
            if (isChatSystemPush(bundle)) {
                ChatController.getInstance(context).onSystemMessageFromServer(context, bundle, alert);
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

    private boolean isChatSystemPush(final Bundle bundle) {
        final PushMessageTypes messageType = PushMessageTypes.getKnownType(bundle);
        return messageType == PushMessageTypes.TYPING
                || messageType == PushMessageTypes.MESSAGES_READ
                || messageType == PushMessageTypes.REMOVE_PUSHES
                || messageType == PushMessageTypes.UNREAD_MESSAGE_NOTIFICATION;
    }
}
