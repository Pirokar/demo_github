package im.threads.push;

import com.google.firebase.iid.FirebaseInstanceId;
import com.mfms.android.push_lite.PushInstanceIDListenerService;

import im.threads.internal.Config;
import im.threads.internal.utils.PrefUtils;

/**
 * TODO THREADS-6293: FirebaseInstanceIdService is deprecated!
 */
public class ThreadsPushInstanceIDListenerService extends PushInstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        if (Config.instance.transport.getType() == Config.TransportType.MFMS_PUSH) {
            super.onTokenRefresh();
        }
        PrefUtils.setFcmToken(FirebaseInstanceId.getInstance().getToken());
    }
}
