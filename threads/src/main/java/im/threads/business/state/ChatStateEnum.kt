package im.threads.business.state

enum class ChatStateEnum(val value: Int) {
    LOGGED_OUT(0),
    LOGGING_IN(1),
    REGISTERING_DEVICE(2),
    DEVICE_REGISTERED(3),
    SENDING_INIT_USER(4),
    INIT_USER_SENT(5),
    ATTACHMENT_SETTINGS_LOADED(6),
    HISTORY_LOADED(7);

    fun isLastObservableState() = this == ATTACHMENT_SETTINGS_LOADED
}
