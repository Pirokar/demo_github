package im.threads.business.utils

import im.threads.business.models.ChatItem

object ChatItemListFinder {
    @JvmStatic
    fun indexOf(list: List<ChatItem?>, chatItem: ChatItem): Int {
        return list.indexOfFirst { it?.isTheSameItem(chatItem) ?: false }
    }

    @JvmStatic
    fun lastIndexOf(list: List<ChatItem?>, chatItem: ChatItem): Int {
        return list.indexOfLast { it?.isTheSameItem(chatItem) ?: false }
    }
}
