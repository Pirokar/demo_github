package im.threads.internal.formatters

enum class ChatItemType {
    TYPING, ATTACHMENT_SETTINGS,

    // incoming
    SCHEDULE, SURVEY, REQUEST_CLOSE_THREAD, MESSAGE, ON_HOLD, NONE, MESSAGES_READ, CLIENT_BLOCKED, SCENARIO,

    // system
    THREAD_ENQUEUED, AVERAGE_WAIT_TIME, PARTING_AFTER_SURVEY, OPERATOR_JOINED, THREAD_CLOSED, THREAD_WILL_BE_REASSIGNED, THREAD_IN_PROGRESS, OPERATOR_LEFT, OPERATOR_LOOKUP_STARTED,

    // outgoing
    INIT_CHAT, CLIENT_INFO, SURVEY_QUESTION_ANSWER, SURVEY_PASSED, CLOSE_THREAD, REOPEN_THREAD, CLIENT_OFFLINE, SPEECH_MESSAGE_UPDATED, UPDATE_LOCATION, UNKNOWN;

    companion object {
        @JvmStatic
        fun fromString(name: String?): ChatItemType {
            return try {
                valueOf(name!!)
            } catch (ex: IllegalArgumentException) {
                UNKNOWN
            }
        }
    }
}
