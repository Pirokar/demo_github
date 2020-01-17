package im.threads.internal.transport.mfms_push;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.gson.JsonObject;
import com.mfms.android.push_lite.repo.push.remote.model.PushMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import im.threads.internal.Config;
import im.threads.internal.model.ChatItem;
import im.threads.internal.transport.MessageParser;
import im.threads.internal.utils.ThreadsLogger;

public final class MFMSPushMessageParser {
    private static final String TAG = "MFMSPushMessageParser ";

    private MFMSPushMessageParser() {
    }

    public static boolean isThreadsOriginPush(PushMessage pushMessage) {
        final JsonObject fullMessage = getFullMessage(pushMessage);
        return fullMessage != null && isThreadsOriginPush(fullMessage);
    }

    public static boolean isThreadsOriginPush(final Bundle bundle) {
        return bundle != null && PushMessageAttributes.THREADS.equalsIgnoreCase(bundle.getString(PushMessageAttributes.ORIGIN));
    }

    public static List<ChatItem> formatMessages(final List<PushMessage> messages) {
        final List<ChatItem> list = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            final ChatItem chatItem = format(messages.get(i));
            if (chatItem != null) {
                list.add(chatItem);
            }
        }
        return list;
    }

    public static List<String> getReadIds(final Bundle b) {
        final ArrayList<String> ids = new ArrayList<>();
        try {
            if (b == null) return new ArrayList<>();
            final Object readIds = b.get(PushMessageAttributes.READ_PROVIDER_IDS);

            if (readIds instanceof ArrayList) {
                ThreadsLogger.i(TAG, "getReadIds instanceof ArrayList");
                final Collection<? extends String> readInMessageIds = (Collection<? extends String>) b.get(PushMessageAttributes.READ_PROVIDER_IDS);
                if (readInMessageIds != null) {
                    ids.addAll(readInMessageIds);
                }
                ThreadsLogger.e(TAG, "getReadIds = ");
            }
            if (readIds instanceof String) {
                ThreadsLogger.i(TAG, "getReadIds instanceof String " + readIds);
                final String contents = (String) readIds;
                if (!contents.contains(",")) {
                    ids.add((String) readIds);
                } else {
                    final String[] idsArray = contents.replaceAll("\\[", "").replaceAll("]", "").split(",");
                    ids.addAll(Arrays.asList(idsArray));
                }
            }
        } catch (final Exception e) {
            ThreadsLogger.e(TAG, "getReadIds", e);
        }
        return ids;
    }

    public static boolean checkId(final PushMessage pushMessage, final String currentClientId) {
        return MessageParser.checkId(getFullMessage(pushMessage), currentClientId);
    }

    public static String getAppMarker(PushMessage pushMessage) {
        final JsonObject fullMessage = getFullMessage(pushMessage);
        if (fullMessage != null && fullMessage.has(PushMessageAttributes.APP_MARKER_KEY)) {
            return fullMessage.get(PushMessageAttributes.APP_MARKER_KEY).getAsString();
        }
        return null;
    }

    /**
     * @return null, если не удалось распознать формат сообщения.
     */
    @Nullable
    public static ChatItem format(final PushMessage pushMessage) {
        return MessageParser.format(pushMessage.messageId, pushMessage.sentAt, pushMessage.shortMessage, Config.instance.gson.fromJson(pushMessage.fullMessage, JsonObject.class));
    }

    @Nullable
    private static JsonObject getFullMessage(final PushMessage pushMessage) {
        if (pushMessage.fullMessage != null) {
            return Config.instance.gson.fromJson(pushMessage.fullMessage, JsonObject.class);
        }
        return null;
    }

    private static boolean isThreadsOriginPush(final JsonObject fullMessage) {
        return PushMessageAttributes.THREADS.equalsIgnoreCase(fullMessage.get(PushMessageAttributes.ORIGIN).getAsString());
    }
}
