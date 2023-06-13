package im.threads.business.state

enum class ChatStateEnum(val value: Int) {
    LOGGED_OUT(0),
    REGISTERING_DEVICE(1),
    DEVICE_REGISTERED(2),
    SENDING_INIT_USER(3),
    INIT_USER_SENT(4),
    LOADING_SETTINGS(5),
    SETTINGS_LOADED(6),
    HISTORY_LOADED(7)
}
