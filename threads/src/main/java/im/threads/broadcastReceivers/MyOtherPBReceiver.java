package im.threads.broadcastReceivers;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.pushserver.android.PushBroadcastReceiver;

/**
 * Created by yuri on 22.06.2016.
 */
public class MyOtherPBReceiver extends PushBroadcastReceiver {
    private static final String TAG = "MyOtherPBReceiver ";

    @Override
    public void onNewPushNotification(Context context, String s, Bundle bundle) {
        Log.i(TAG, "onNewPushNotification " + s + " " + bundle);
    }

    @Override
    public void onStatusChanged(Context context, String s) {
        Log.i(TAG, "onStatusChanged " + s);

    }

    @Override
    public void onDeviceAddressChanged(Context context, String s) {
        Log.i(TAG, "onDeviceAddressChanged " + s);

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
