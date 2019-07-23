package im.threads.broadcastReceivers;

import android.content.Context;
import android.os.Bundle;

import com.mfms.android.push_lite.PushBroadcastReceiver;

import im.threads.internal.ThreadsLogger;

/**
 * Created by yuri on 22.06.2016.
 */
public class MyOtherPBReceiver extends PushBroadcastReceiver {
    private static final String TAG = "MyOtherPBReceiver ";

    @Override
    public void onNewPushNotification(Context context, String s, Bundle bundle) {
        ThreadsLogger.i(TAG, "onNewPushNotification " + s + " " + bundle);
    }

    @Override
    public void onStatusChanged(Context context, String s) {
        ThreadsLogger.i(TAG, "onStatusChanged " + s);
    }

    @Override
    public void onDeviceAddressChanged(Context context, String s) {
        ThreadsLogger.i(TAG, "onDeviceAddressChanged " + s);
    }

    @Override
    public void onDeviceAddressProblems(Context context, String s) {
        ThreadsLogger.w(TAG, "onDeviceAddressProblems " + s);
    }

    @Override
    public void onError(Context context, String s) {
        ThreadsLogger.e(TAG, "onFileDonwloaderError " + s);
    }
}
