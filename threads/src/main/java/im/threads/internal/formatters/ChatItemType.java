package im.threads.internal.formatters;

import android.support.annotation.NonNull;

public enum ChatItemType {
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
    THREAD_CLOSED,
    UNREAD_MESSAGE_NOTIFICATION,
    OPERATOR_LOOKUP_STARTED,
    CLIENT_BLOCKED,
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

    @NonNull
    public static ChatItemType fromString(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException ex) {
            return ChatItemType.UNKNOWN;
        }
    }
}


