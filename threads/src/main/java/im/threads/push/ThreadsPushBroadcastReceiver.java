package im.threads.push;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.edna.android.push_lite.PushBroadcastReceiver;
import com.edna.android.push_lite.repo.push.remote.model.PushMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.Nullable;
import im.threads.internal.chat_updates.ChatUpdateProcessor;
import im.threads.internal.formatters.MessageFormatter;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ScheduleInfo;
import im.threads.internal.model.SpeechMessageUpdate;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.services.NotificationService;
import im.threads.internal.transport.mfms_push.MFMSPushMessageParser;
import im.threads.internal.transport.mfms_push.ShortPushMessageProcessingDelegate;
import im.threads.internal.utils.PrefUtils;
import im.threads.internal.utils.ThreadsLogger;

/**
 * Приемщик pushLite-уведомлений
 */
public class ThreadsPushBroadcastReceiver extends PushBroadcastReceiver {
    private static final String TAG = "ThreadsPushBroadcastReceiver ";

    @Override
    protected void onShortPushReceived(Context context, @Nullable String messageId, @Nullable String alert, Bundle bundle) {
        ThreadsLogger.i(TAG, "onNewPushNotification " + alert + " " + bundle);
        ShortPushMessageProcessingDelegate.INSTANCE.process(context, bundle, alert);
    }

    @Override
    protected boolean onLongPushReceived(Context context, List<PushMessage> pushMessages) {
        ThreadsLogger.i(TAG, "saveMessages " + pushMessages);
        // В контроллер чата уходят только распознанные по формату чата сообщения.
        // Остальные уходят на обработку пользователям библиотеки.
        List<PushMessage> toShow = new ArrayList<>();
        for (int i = 0; i < pushMessages.size(); i++) {
            PushMessage pushMessage = pushMessages.get(i);
            if (MFMSPushMessageParser.isThreadsOriginPush(pushMessage)) {
                boolean isCurrentClientId = MFMSPushMessageParser.checkId(pushMessage, PrefUtils.getClientID());
                final ChatItem chatItem = MFMSPushMessageParser.format(pushMessage);
                if (chatItem != null) {
                    if (isCurrentClientId) {
                        ChatUpdateProcessor.getInstance().postNewMessage(chatItem);
                    }
                    if (chatItem instanceof SpeechMessageUpdate) {
                        ChatUpdateProcessor.getInstance().postSpeechMessageUpdate((SpeechMessageUpdate) chatItem);
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
                MessageFormatter.MessageContent messageContent = MessageFormatter.parseMessageContent(context, chatItems);
                NotificationService.addUnreadMessageList(context, appMarker, messageContent);
            }
        }
        return true;
    }

    @Override
    public void onStatusChanged(final Context context, final String s) {
        ThreadsLogger.e(TAG, "onStatusChanged " + s);
    }

    @Override
    public void onDeviceAddressChanged(final Context context, final String s) {
        ThreadsLogger.i(TAG, "onDeviceAddressChanged " + s);
        ChatUpdateProcessor.getInstance().postDeviceAddressChanged(s);
    }

    @Override
    public void onDeviceAddressProblems(final Context context, final String s) {
        ThreadsLogger.w(TAG, "onDeviceAddressProblems " + s);
    }

    @Override
    public void onError(final Context context, final String s) {
        ThreadsLogger.e(TAG, "onFileDonwloaderError " + s);
    }
}
