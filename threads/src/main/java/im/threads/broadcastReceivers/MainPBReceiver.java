package im.threads.broadcastReceivers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.pushserver.android.PushBroadcastReceiver;
import im.threads.utils.MessageMatcher;
import im.threads.controllers.ChatController;
import im.threads.utils.PrefUtils;

/**
 * Created by yuri on 22.06.2016.
 */
public class MainPBReceiver extends PushBroadcastReceiver {
    private static final String TAG = "MainPBReceiver ";

    @Override
    public void onNewPushNotification(Context context, String s, Bundle bundle) {
        Log.i(TAG, "onNewPushNotification " + s + " " + bundle);
        if (MessageMatcher.getType(bundle) == MessageMatcher.TYPE_OPERATOR_TYPING
                || MessageMatcher.getType(bundle) == MessageMatcher.TYPE_MESSAGES_READ)
            ChatController.getInstance(context, PrefUtils.getClientID(context)).onSystemMessageFromServer(context, bundle);
    }

    @Override
    public void onStatusChanged(Context context, String s) {
        Log.e(TAG, "onStatusChanged " + s);
    }

    @Override
    public void onDeviceAddressChanged(final Context context, final String s) {
        Log.i(TAG, "onDeviceAddressChanged " + s);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (s != null) PrefUtils.setDeviceAddress(context, s);
                context.sendBroadcast(new Intent(ChatController.DEVICE_ID_IS_SET_BROADCAST));
            }
        }).start();
    }

    @Override
    public void onDeviceAddressProblems(Context context, String s) {
        Log.i(TAG, "onDeviceAddressProblems " + s);
    }

    @Override
    public void onError(Context context, String s) {
        Log.e(TAG, "onFileDonwloaderError " + s);
    }
}
