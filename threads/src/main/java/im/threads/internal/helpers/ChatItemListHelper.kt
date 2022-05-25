package im.threads.internal.helpers

import im.threads.internal.model.ChatItem

object ChatItemListHelper {
    @JvmStatic
    fun indexOf(list: List<ChatItem?>, chatItem: ChatItem): Int {
        for (i in list.indices) {
            if (chatItem.isTheSameItem(list[i])) {
                return i
            }
        }
        return -1
    }

    @JvmStatic
    fun lastIndexOf(list: List<ChatItem?>, chatItem: ChatItem): Int {
        for (i in list.indices.reversed()) {
            if (chatItem.isTheSameItem(list[i])) {
                return i
            }
        }
        return -1
    }
}
