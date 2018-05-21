package im.threads.broadcastReceivers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.pushserver.android.PushBroadcastReceiver;

import im.threads.controllers.ChatController;
import im.threads.formatters.IncomingMessageParser;
import im.threads.formatters.PushMessageTypes;
import im.threads.model.ChatStyle;

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
        if (ChatStyle.getInstance().isDebugLoggingEnabled) {
            Log.i(TAG, "onNewPushNotification " + s + " " + bundle);
        }

        if (IncomingMessageParser.isThreadsOriginPush(bundle)) {

            if (isChatSystemPush(bundle)) {
                ChatController.getInstance(context).onSystemMessageFromServer(context, bundle, s);
            }

        } else if (ChatController.getShortPushListener() != null) {
            ChatController.getShortPushListener().onNewShortPushNotification(this, context, s, bundle);
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
        Log.e(TAG, "onStatusChanged " + s);
    }

    @Override
    public void onDeviceAddressChanged(final Context context, final String s) {

        if (ChatStyle.getInstance().isDebugLoggingEnabled) {
            Log.i(TAG, "onDeviceAddressChanged " + s);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                context.sendBroadcast(new Intent(ProgressReceiver.DEVICE_ID_IS_SET_BROADCAST));
            }
        }).start();
    }

    @Override
    public void onDeviceAddressProblems(final Context context, final String s) {
        Log.w(TAG, "onDeviceAddressProblems " + s);
    }

    @Override
    public void onError(final Context context, final String s) {
        Log.e(TAG, "onFileDonwloaderError " + s);
    }
}
