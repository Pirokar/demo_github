package im.threads.formatters;

import android.os.Bundle;
import android.text.TextUtils;

public enum PushMessageTypes {
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

    public static PushMessageTypes getKnownType(Bundle bundle) {
        if (bundle == null) {
            return UNKNOWN;
        }
        if (bundle.containsKey("readInMessageIds")) {
            return MESSAGES_READ;
        }

        String pushType = bundle.getString(PushMessageAttributes.TYPE);

        if (!TextUtils.isEmpty(pushType)) {
            PushMessageTypes pushMessageTypes = valueOf(pushType);

            if (pushMessageTypes == OPERATOR_JOINED ||
                    pushMessageTypes == OPERATOR_LEFT ||
                    pushMessageTypes == TYPING ||
                    pushMessageTypes == SCHEDULE ||
                    pushMessageTypes == SURVEY ||
                    pushMessageTypes == REQUEST_CLOSE_THREAD ||
                    pushMessageTypes == REMOVE_PUSHES ||
                    pushMessageTypes == UNREAD_MESSAGE_NOTIFICATION) {
                return pushMessageTypes;
            }
        }

       // old push format
        if (pushType == null && bundle.getString("alert") != null && bundle.getString("advisa") == null && bundle.getString("GEO_FENCING") == null) {
            return MESSAGE;
        }
        if (bundle.getString("origin") != null && "threads".equalsIgnoreCase(bundle.getString("origin"))){
            return CHAT_PUSH;
        }

        return UNKNOWN;
    }
}


