package im.threads.services;

import android.content.Intent;
import android.util.Log;

import com.mfms.android.push_lite.PushServerIntentService;
import com.mfms.android.push_lite.repo.push.remote.model.PushMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.threads.controllers.ChatController;
import im.threads.formatters.IncomingMessageParser;
import im.threads.internal.Config;
import im.threads.model.ChatStyle;
import im.threads.model.PushMessageCheckResult;

public class IncomingMessagesIntentService extends PushServerIntentService {

    private static final String TAG = "MessagesIntentService ";

    @Override
    protected boolean saveMessages(List<PushMessage> list) {
        if (ChatStyle.getInstance().isDebugLoggingEnabled) {
            Log.i(TAG, "saveMessages " + list);
        }
        if (list == null) return false;
        // В контроллер чата уходят только распознанные по формату чата сообщения.
        // Остальные уходят на обработку пользователям библиотеки.
        List<PushMessage> toShow = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            PushMessage pushMessage = list.get(i);
            if (IncomingMessageParser.isThreadsOriginPush(pushMessage)) {
                ChatController chatController = ChatController.getInstance(getApplication());
                PushMessageCheckResult result = chatController.onFullMessage(pushMessage);
                if (result.isDetected()) {
                    if (result.isNeedsShowIsStatusBar()) {
                        toShow.add(pushMessage);
                    }
                }
            } else if (Config.instance.fullPushListener != null) {
                Config.instance.fullPushListener.onNewFullPushNotification(this, pushMessage);
            }
        }
        if (toShow.size() > 0) {
            HashMap<String, ArrayList<PushMessage>> appMarkerMessagesMap = new HashMap<>();
            for (PushMessage pushMessage : toShow) {
                String appMarker = IncomingMessageParser.getAppMarker(pushMessage);
                if (!appMarkerMessagesMap.containsKey(appMarker)) {
                    appMarkerMessagesMap.put(appMarker, new ArrayList<PushMessage>());
                }
                appMarkerMessagesMap.get(appMarker).add(pushMessage);
            }
            for (String appMarker : appMarkerMessagesMap.keySet()) {
                Intent intent = new Intent(getApplicationContext(), NotificationService.class);
                ArrayList<PushMessage> al = new ArrayList<>(appMarkerMessagesMap.get(appMarker));
                intent.putParcelableArrayListExtra(NotificationService.ACTION_ADD_UNREAD_MESSAGE, al);
                intent.putExtra(NotificationService.EXTRA_APP_MARKER, appMarker);
                intent.setAction(NotificationService.ACTION_ADD_UNREAD_MESSAGE);
                startService(intent);
            }
        }
        return true;
    }

    @Override
    protected void messagesWereRead(List<String> list) {
        if (ChatStyle.getInstance().isDebugLoggingEnabled) {
            Log.e(TAG, "messagesWereRead " + list);
        }
        //TODO THREADS-3937 Why it is not used?
    }
}
