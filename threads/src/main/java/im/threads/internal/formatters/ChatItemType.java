package im.threads.internal.formatters;

import androidx.annotation.NonNull;

public enum ChatItemType {
    TYPING,
    ATTACHMENT_SETTINGS,
    // incoming
    SCHEDULE,
    SURVEY,
    REQUEST_CLOSE_THREAD,
    MESSAGE,
    ON_HOLD,
    NONE,
    MESSAGES_READ,
    REMOVE_PUSHES,
    UNREAD_MESSAGE_NOTIFICATION,
    CLIENT_BLOCKED,
    SCENARIO,
    CHAT_PUSH,
    //system
    THREAD_ENQUEUED,
    AVERAGE_WAIT_TIME,
    PARTING_AFTER_SURVEY,
    OPERATOR_JOINED,
    THREAD_CLOSED,
    THREAD_WILL_BE_REASSIGNED,
    THREAD_IN_PROGRESS,
    @Deprecated
    OPERATOR_LEFT,
    @Deprecated
    OPERATOR_LOOKUP_STARTED,

    // outgoing
    INIT_CHAT,
    CLIENT_INFO,
    SURVEY_QUESTION_ANSWER,
    SURVEY_PASSED,
    CLOSE_THREAD,
    REOPEN_THREAD,
    CLIENT_OFFLINE,
    SPEECH_MESSAGE_UPDATED,

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
