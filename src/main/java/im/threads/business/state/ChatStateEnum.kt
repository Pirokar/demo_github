package im.threads.business.state

enum class ChatStateEnum(val value: Int, var isTimeOut: Boolean) {
    LOGGED_OUT(0, isTimeOut = false),
    REGISTERING_DEVICE(1, isTimeOut = false),
    DEVICE_REGISTERED(2, isTimeOut = false),
    SENDING_INIT_USER(3, isTimeOut = false),
    INIT_USER_SENT(4, isTimeOut = false),
    HISTORY_LOADED(5, isTimeOut = false)
}
