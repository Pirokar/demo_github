package im.threads.business.state

enum class ChatStateEnum(val value: Int) {
    LOGGED_OUT(0),
    REGISTERING_DEVICE(1),
    DEVICE_REGISTERED(2),
    SENDING_INIT_USER(3),
    INIT_USER_SENT(4),
    HISTORY_LOADED(5)
}
