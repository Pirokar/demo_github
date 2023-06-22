package im.threads.ui.adapters.utils

import im.threads.business.models.ChatItem
import im.threads.business.models.SimpleSystemMessage

/**
 * Удаляет дубликаты сообщений
 */
class DuplicateMessagesRemover {
    companion object {
        /**
         * Удаляет дубликаты системных сообщений по тексту
         * @param listWhere список, в котором будут удалены дубликаты
         */
        @JvmStatic
        fun removeDuplicateSystemMessages(listWhere: MutableList<ChatItem>) {
            val mapOfLastElements = HashMap<String, Int>()
            val elementsToRemove = arrayListOf<ChatItem>()
            listWhere.indices.forEach { index ->
                (listWhere[index] as? SimpleSystemMessage)?.let { mapOfLastElements[it.getText()] = index }
            }
            listWhere.indices.forEach { index ->
                (listWhere[index] as? SimpleSystemMessage)?.let {
                    if (mapOfLastElements[it.getText()] != index) {
                        elementsToRemove.add(it)
                    }
                }
            }
            elementsToRemove.distinct()
            elementsToRemove.forEach { listWhere.remove(it) }
        }
    }
}
