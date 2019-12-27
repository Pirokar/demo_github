package im.threads.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.mfms.android.push_lite.PushBroadcastReceiver;

import java.util.List;

import androidx.annotation.NonNull;
import im.threads.internal.broadcastReceivers.ProgressReceiver;
import im.threads.internal.chat_updates.ChatUpdateProcessor;
import im.threads.internal.formatters.ChatItemType;
import im.threads.internal.services.NotificationService;
import im.threads.internal.transport.mfms_push.MFMSPushMessageParser;
import im.threads.internal.transport.mfms_push.PushMessageAttributes;
import im.threads.internal.utils.ThreadsLogger;

/**
 * Приемщик всех коротких пуш уведомлений,
 * т.е. просто всех уведомлений напрямую от GCM.
 * Полная информация о пуш уведомлениях скачивается отдельно
 * и доступна в {@link ThreadsPushServerIntentService}
 */
public class ThreadsPushBroadcastReceiver extends PushBroadcastReceiver {
    private static final String TAG = "ThreadsPushBroadcastReceiver ";

    @Override
    protected void onNewPushNotification(final Context context, final String alert, final Bundle bundle) {
        ThreadsLogger.i(TAG, "onNewPushNotification " + alert + " " + bundle);
        if (MFMSPushMessageParser.isThreadsOriginPush(bundle)) {
            final ChatItemType chatMessageType = getKnownType(bundle);
            switch (chatMessageType) {
                case TYPING:
                    String clientId = bundle.getString(PushMessageAttributes.CLIENT_ID);
                    if (clientId != null) {
                        ChatUpdateProcessor.getInstance().postTyping(clientId);
                    }
                    break;
                case MESSAGES_READ:
                    final List<String> list = MFMSPushMessageParser.getReadIds(bundle);
                    ThreadsLogger.i(TAG, "onSystemMessageFromServer: read messages " + list);
                    for (final String readMessageProviderId : list) {
                        ChatUpdateProcessor.getInstance().postUserMessageWasRead(readMessageProviderId);
                    }
                    break;
                case REMOVE_PUSHES:
                    NotificationService.removeNotification(context);
                    break;
                case UNREAD_MESSAGE_NOTIFICATION:
                    String operatorUrl = bundle.getString(PushMessageAttributes.OPERATOR_URL);
                    String appMarker = bundle.getString(PushMessageAttributes.APP_MARKER_KEY);
                    NotificationService.addUnreadMessage(context, alert, operatorUrl, appMarker);
                    break;
            }
        }
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

    @NonNull
    private ChatItemType getKnownType(final Bundle bundle) {
        if (bundle == null) {
            return ChatItemType.UNKNOWN;
        }
        if (bundle.containsKey(PushMessageAttributes.READ_PROVIDER_IDS)) {
            return ChatItemType.MESSAGES_READ;
        }
        final String pushType = bundle.getString(PushMessageAttributes.TYPE);
        if (!TextUtils.isEmpty(pushType)) {
            final ChatItemType chatItemType = ChatItemType.fromString(pushType);
            if (chatItemType == ChatItemType.OPERATOR_JOINED ||
                    chatItemType == ChatItemType.OPERATOR_LEFT ||
                    chatItemType == ChatItemType.TYPING ||
                    chatItemType == ChatItemType.SCHEDULE ||
                    chatItemType == ChatItemType.SURVEY ||
                    chatItemType == ChatItemType.REQUEST_CLOSE_THREAD ||
                    chatItemType == ChatItemType.REMOVE_PUSHES ||
                    chatItemType == ChatItemType.UNREAD_MESSAGE_NOTIFICATION) {
                return chatItemType;
            }
        }
        // old push format
        if (pushType == null && bundle.getString("alert") != null && bundle.getString("advisa") == null && bundle.getString("GEO_FENCING") == null) {
            return ChatItemType.MESSAGE;
        }
        if (MFMSPushMessageParser.isThreadsOriginPush(bundle)) {
            return ChatItemType.CHAT_PUSH;
        }
        return ChatItemType.UNKNOWN;
    }
}
