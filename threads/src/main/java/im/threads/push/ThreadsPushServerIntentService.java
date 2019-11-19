package im.threads.push;

import android.text.TextUtils;

import com.mfms.android.push_lite.PushServerIntentService;
import com.mfms.android.push_lite.repo.push.remote.model.PushMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.threads.internal.chat_updates.ChatUpdateProcessor;
import im.threads.internal.transport.mfms_push.MFMSPushMessageParser;
import im.threads.internal.formatters.MessageFormatter;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ScheduleInfo;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.services.NotificationService;
import im.threads.internal.utils.PrefUtils;
import im.threads.internal.utils.ThreadsLogger;

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
            if (MFMSPushMessageParser.isThreadsOriginPush(pushMessage)) {
                boolean isCurrentClientId = MFMSPushMessageParser.checkId(pushMessage, PrefUtils.getClientID());
                final ChatItem chatItem = MFMSPushMessageParser.format(pushMessage);
                if (chatItem != null) {
                    if (isCurrentClientId) {
                        ChatUpdateProcessor.getInstance().postNewMessage(chatItem);
                    }
                    if (!(chatItem instanceof ScheduleInfo) && !(chatItem instanceof UserPhrase) && !TextUtils.isEmpty(pushMessage.shortMessage)) {
                        toShow.add(pushMessage);
                    }
                }
            }
        }
        if (toShow.size() > 0) {
            HashMap<String, ArrayList<PushMessage>> appMarkerMessagesMap = new HashMap<>();
            for (PushMessage pushMessage : toShow) {
                String appMarker = MFMSPushMessageParser.getAppMarker(pushMessage);
                if (!appMarkerMessagesMap.containsKey(appMarker)) {
                    appMarkerMessagesMap.put(appMarker, new ArrayList<>());
                }
                appMarkerMessagesMap.get(appMarker).add(pushMessage);
            }
            for (String appMarker : appMarkerMessagesMap.keySet()) {
                List<ChatItem> chatItems = MFMSPushMessageParser.formatMessages(appMarkerMessagesMap.get(appMarker));
                MessageFormatter.MessageContent messageContent = MessageFormatter.parseMessageContent(getApplicationContext(), chatItems);
                NotificationService.addUnreadMessageList(getApplicationContext(), appMarker, messageContent);
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
