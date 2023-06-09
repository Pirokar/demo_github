package im.threads.business.state

enum class ChatState(val value: Int) {
    LOGGED_OUT(0), INIT_USER(1), ANDROID_CHAT_LIFECYCLE(2)
}
