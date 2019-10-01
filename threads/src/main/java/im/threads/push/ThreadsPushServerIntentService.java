package im.threads.push;

import android.content.Intent;

import com.mfms.android.push_lite.PushServerIntentService;
import com.mfms.android.push_lite.repo.push.remote.model.PushMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.threads.internal.controllers.ChatController;
import im.threads.internal.formatters.IncomingMessageParser;
import im.threads.internal.utils.ThreadsLogger;
import im.threads.internal.model.PushMessageCheckResult;
import im.threads.internal.services.NotificationService;

public class ThreadsPushServerIntentService extends PushServerIntentService {

    private static final String TAG = "ThreadsPushServerIntentService ";

    @Override
    protected boolean saveMessages(List<PushMessage> list) {
        ThreadsLogger.i(TAG, "saveMessages " + list);
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
            }
        }
        if (toShow.size() > 0) {
            HashMap<String, ArrayList<PushMessage>> appMarkerMessagesMap = new HashMap<>();
            for (PushMessage pushMessage : toShow) {
                String appMarker = IncomingMessageParser.getAppMarker(pushMessage);
                if (!appMarkerMessagesMap.containsKey(appMarker)) {
                    appMarkerMessagesMap.put(appMarker, new ArrayList<>());
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
        ThreadsLogger.e(TAG, "messagesWereRead " + list);
        //TODO THREADS-3937 Why it is not used?
    }
}
