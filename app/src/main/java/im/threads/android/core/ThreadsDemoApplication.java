package im.threads.android.core;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.mfms.android.push_lite.PushBroadcastReceiver;
import com.mfms.android.push_lite.PushServerIntentService;
import com.mfms.android.push_lite.repo.push.remote.model.PushMessage;

import java.util.List;

import im.threads.ThreadsLib;
import im.threads.android.data.Card;
import im.threads.android.ui.BottomNavigationActivity;
import im.threads.android.utils.ChatBuilderHelper;
import im.threads.android.utils.PrefUtils;
import io.reactivex.subjects.BehaviorSubject;

public class ThreadsDemoApplication extends MultiDexApplication {

    private static Context appContext;

    private static BehaviorSubject<Integer> unreadMessagesSubject = BehaviorSubject.create();

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        ThreadsLib.ConfigBuilder config = new ThreadsLib.ConfigBuilder(this)
                .pendingIntentCreator(new CustomPendingIntentCreator())
                .shortPushListener(new CustomShortPushListener())
                .fullPushListener(new CustomFullPushListener())
                .unreadMessagesCountListener(count -> unreadMessagesSubject.onNext(count));
        ThreadsLib.init(config);
    }

    public static Context getAppContext() {
        return appContext;
    }

    public static BehaviorSubject<Integer> getUnreadMessagesSubject() {
        return unreadMessagesSubject;
    }

    private static class CustomPendingIntentCreator implements ThreadsLib.PendingIntentCreator {
        @Override
        public PendingIntent create(Context context, String appMarker) {
            if (!TextUtils.isEmpty(appMarker)) {
                //This is an example of creating pending intent for multi-chat app
                List<Card> clientCards = PrefUtils.getCards(context);
                Card pushClientCard = null;
                for (Card clientCard : clientCards) {
                    if (appMarker.equalsIgnoreCase(clientCard.getAppMarker())) {
                        pushClientCard = clientCard;
                    }
                }
                if (pushClientCard != null) {
                    ChatBuilderHelper.ChatDesign chatDesign = ChatBuilderHelper.ChatDesign.BLUE;
                    if (appMarker.endsWith("CRG")) {
                        chatDesign = ChatBuilderHelper.ChatDesign.GREEN;
                    }
                    return BottomNavigationActivity.createPendingIntent(context,
                            pushClientCard.getUserId(), pushClientCard.getUserName(),
                            pushClientCard.getAppMarker(), pushClientCard.getClientIdSignature(),
                            chatDesign);
                }
            } else {
                //This is an example of creating pending intent for single-chat app
                List<Card> clientCards = PrefUtils.getCards(context);
                if (!clientCards.isEmpty()) {
                    Card pushClientCard = clientCards.get(0);
                    return BottomNavigationActivity.createPendingIntent(context,
                            pushClientCard.getUserId(), pushClientCard.getUserName(),
                            pushClientCard.getAppMarker(), pushClientCard.getClientIdSignature(),
                            ChatBuilderHelper.ChatDesign.GREEN);
                }
            }
            return null;
        }
    }

    private static class CustomShortPushListener implements ThreadsLib.ShortPushListener {

        private static final String TAG = "CustomShortPushListener";

        @Override
        public void onNewShortPushNotification(PushBroadcastReceiver pushBroadcastReceiver, Context context, String s, Bundle bundle) {
            Log.i(TAG, "Short push not accepted by chat: " + bundle.toString());
            Toast.makeText(context, "Short push not accepted by chat: " + bundle.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private static class CustomFullPushListener implements ThreadsLib.FullPushListener {

        private static final String TAG = "CustomFullPushListener";

        @Override
        public void onNewFullPushNotification(PushServerIntentService pushServerIntentService, PushMessage pushMessage) {
            Toast.makeText(pushServerIntentService.getApplicationContext(), "Full push not accepted by chat: " + pushMessage, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Full push not accepted by chat: " + pushMessage);
        }
    }
}
