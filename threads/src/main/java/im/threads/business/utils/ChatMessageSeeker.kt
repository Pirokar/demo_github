package im.threads.business.utils

import im.threads.business.models.ChatItem
import im.threads.business.models.ChatPhrase
import im.threads.business.utils.ChatItemListFinder.lastIndexOf
import java.util.Locale

class ChatMessageSeeker {
    private var lastQuery = ""
    private var lastHighlightedItem: ChatItem? = null

    fun searchMessages(
        target: List<ChatItem>,
        forward: Boolean,
        query: String
    ): Pair<List<ChatItem>, ChatItem?> {
        if (target.isEmpty()) return Pair(target, null)
        if (query.isEmpty()) {
            lastQuery = ""
            lastHighlightedItem = null
            return Pair(target, null)
        }
        var lastHighlightedIndex = -1
        if (lastQuery == query) {
            lastHighlightedItem?.let {
                lastHighlightedIndex = lastIndexOf(target, it)
            }
        }
        lastQuery = query
        // для поиска сообщений в чате - пробегаемся по всем сообщениям и отмечаем
        // те, которые соответствуют запросу
        for (chatItem in target) {
            if (chatItem is ChatPhrase) {
                chatItem.found =
                    chatItem.phraseText?.lowercase(Locale.getDefault())?.contains(query.lowercase()) ?: false
            }
        }
        return if (forward) {
            if (lastHighlightedIndex == 0) { // if it is last
                Pair(target, lastHighlightedItem)
            } else {
                val initial = if (lastHighlightedIndex == -1) target.size - 1 else lastHighlightedIndex - 1
                for (i in initial downTo 0) {
                    if (target[i] is ChatPhrase &&
                        (target[i] as ChatPhrase).phraseText?.lowercase(Locale.getDefault())
                            ?.contains(query.lowercase()) == true
                    ) {
                        lastHighlightedItem = (target[i] as ChatPhrase)
                        return Pair(target, lastHighlightedItem)
                    }
                }
                if (lastHighlightedIndex == -1) {
                    lastHighlightedItem = null
                    Pair(target, null)
                } else {
                    Pair(target, lastHighlightedItem)
                }
            }
        } else {
            if (lastHighlightedIndex == -1) {
                for (i in target.indices.reversed()) {
                    if (target[i] is ChatPhrase &&
                        (target[i] as ChatPhrase).phraseText?.lowercase(Locale.getDefault())
                            ?.contains(query.lowercase()) == true
                    ) {
                        lastHighlightedItem = (target[i] as ChatPhrase)
                        return Pair(target, lastHighlightedItem)
                    }
                }
                lastHighlightedItem = null
                return Pair(target, null)
            }
            for (i in lastHighlightedIndex + 1 until target.size) {
                if (target[i] is ChatPhrase &&
                    (target[i] as ChatPhrase).phraseText?.lowercase(Locale.getDefault())
                        ?.contains(query.lowercase()) == true
                ) {
                    lastHighlightedItem = (target[i] as ChatPhrase)
                    return Pair(target, lastHighlightedItem)
                }
            }
            return Pair(target, lastHighlightedItem)
        }
    }
}
