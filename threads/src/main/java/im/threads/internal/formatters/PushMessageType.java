package im.threads.internal.formatters;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

public enum PushMessageType {
    TYPING,
    // incoming
    OPERATOR_JOINED,
    OPERATOR_LEFT,
    SCHEDULE,
    SURVEY,
    REQUEST_CLOSE_THREAD,
    MESSAGE,
    ON_HOLD,
    NONE,
    MESSAGES_READ,
    REMOVE_PUSHES,
    UNREAD_MESSAGE_NOTIFICATION,
    OPERATOR_LOOKUP_STARTED,
    CLIENT_BLOCKED,
    THREAD_OPENED,
    THREAD_CLOSED,
    SCENARIO,
    CHAT_PUSH,

    // outgoing
    INIT_CHAT,
    CLIENT_INFO,
    SURVEY_QUESTION_ANSWER,
    SURVEY_PASSED,
    CLOSE_THREAD,
    REOPEN_THREAD,
    CLIENT_OFFLINE,

    UNKNOWN;

    public static PushMessageType getKnownType(final Bundle bundle) {
        if (bundle == null) {
            return UNKNOWN;
        }
        if (bundle.containsKey(PushMessageAttributes.READ_PROVIDER_IDS)) {
            return MESSAGES_READ;
        }
        final String pushType = bundle.getString(PushMessageAttributes.TYPE);
        if (!TextUtils.isEmpty(pushType)) {
            final PushMessageType pushMessageType = fromString(pushType);
            if (pushMessageType == OPERATOR_JOINED ||
                    pushMessageType == OPERATOR_LEFT ||
                    pushMessageType == TYPING ||
                    pushMessageType == SCHEDULE ||
                    pushMessageType == SURVEY ||
                    pushMessageType == REQUEST_CLOSE_THREAD ||
                    pushMessageType == REMOVE_PUSHES ||
                    pushMessageType == UNREAD_MESSAGE_NOTIFICATION) {
                return pushMessageType;
            }
        }
        // old push format
        if (pushType == null && bundle.getString("alert") != null && bundle.getString("advisa") == null && bundle.getString("GEO_FENCING") == null) {
            return MESSAGE;
        }
        if (IncomingMessageParser.isThreadsOriginPush(bundle)) {
            return CHAT_PUSH;
        }
        return UNKNOWN;
    }

    @NonNull
    public static PushMessageType fromString(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException ex) {
            return PushMessageType.UNKNOWN;
        }
    }
}


