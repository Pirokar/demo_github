package im.threads.broadcastReceivers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.mfms.android.push_lite.PushBroadcastReceiver;

import im.threads.controllers.ChatController;
import im.threads.formatters.IncomingMessageParser;
import im.threads.formatters.PushMessageTypes;
import im.threads.internal.Config;
import im.threads.internal.ThreadsLogger;

/**
 * Приемщик всех коротких пуш уведомлений,
 * т.е. просто всех уведомлений на прямую от GCM.
 * Полная информация о пуш уведомлениях скачивается отдельно
 * и доступна в IncomingMessagesIntentService.
 * Created by yuri on 22.06.2016.
 */
public class MainPBReceiver extends PushBroadcastReceiver {
    private static final String TAG = "MainPBReceiver ";

    @Override
    public void onNewPushNotification(final Context context, final String s, final Bundle bundle) {
        ThreadsLogger.i(TAG, "onNewPushNotification " + s + " " + bundle);
        if (IncomingMessageParser.isThreadsOriginPush(bundle)) {
            if (isChatSystemPush(bundle)) {
                ChatController.getInstance(context).onSystemMessageFromServer(context, bundle, s);
            }
        } else if (Config.instance.shortPushListener != null) {
            Config.instance.shortPushListener.onNewShortPushNotification(this, context, s, bundle);
        }
    }

    private boolean isChatSystemPush(final Bundle bundle) {
        final PushMessageTypes messageType = PushMessageTypes.getKnownType(bundle);
        return messageType == PushMessageTypes.TYPING
                || messageType == PushMessageTypes.MESSAGES_READ
                || messageType == PushMessageTypes.REMOVE_PUSHES
                || messageType == PushMessageTypes.UNREAD_MESSAGE_NOTIFICATION;
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
