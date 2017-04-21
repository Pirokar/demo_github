package im.threads.services;

import android.content.Intent;
import android.util.Log;

import com.pushserver.android.PushMessage;
import com.pushserver.android.PushServerIntentService;
import im.threads.controllers.ChatController;
import im.threads.utils.PrefUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class IncomingMessagesIntentService extends PushServerIntentService {

    private static final String TAG = "MessagesIntentService ";

    @Override
    protected boolean saveMessages(List<PushMessage> list) {
        Log.i(TAG, "saveMessages " + list);
        if (list == null) return false;
        // В контроллер чата уходят только распознанные по формату чата сообщения.
        // Остальные уходят на обработку пользователям библиотеки.
        List<PushMessage> detectedMessages = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            PushMessage pushMessage = list.get(i);
            ChatController chatController = ChatController.getInstance(getApplication(), PrefUtils.getClientID(getApplication()));
            boolean messageDetected = chatController.onConsultMessage(pushMessage, getApplication());

            if(messageDetected) {
                detectedMessages.add(pushMessage);
            } else if(ChatController.getFullPushListener() != null) {
                ChatController.getFullPushListener().onNewFullPushNotification(this, pushMessage);
            }
        }

        if(detectedMessages.size() > 0) {
            Intent intent = new Intent(getApplicationContext(), NotificationService.class);
            ArrayList<PushMessage> al = new ArrayList<>(detectedMessages);
            intent.putParcelableArrayListExtra(NotificationService.ACTION_ADD_UNREAD_MESSAGE, al);
            intent.setAction(NotificationService.ACTION_ADD_UNREAD_MESSAGE);
            startService(intent);
        }

        return true;
    }

    @Override
    protected void messagesWereRead(List<String> list) {
        Log.e(TAG, "messagesWereRead " + list);
    }
}
