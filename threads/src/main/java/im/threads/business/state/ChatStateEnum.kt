package im.threads.business.state

enum class ChatStateEnum(val value: Int) {
    LOGGED_OUT(0),
    LOGGING_IN(1),
    REGISTERING_DEVICE(2),
    DEVICE_REGISTERED(3),
    SETTINGS_LOADED(4),
    SENDING_INIT_USER(5),
    INIT_USER_SENT(6),
    HISTORY_LOADED(7),
    THREAD_OPENED(8);

    fun isLastObservableState() = this == THREAD_OPENED
}
