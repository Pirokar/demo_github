package im.threads.business.state

data class ChatStateEvent(val state: ChatStateEnum, val isTimeout: Boolean = false)
