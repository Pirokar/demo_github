package im.threads.business.core

interface UnreadMessagesCountListener {
    fun onUnreadMessagesCountChanged(count: Int)
}
