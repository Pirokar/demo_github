package im.threads.business.database

import im.threads.business.models.ChatItem
import im.threads.business.models.FileDescription

interface DBHelper {
    fun cleanDatabase()
    fun getChatItems(offset: Int, limit: Int): List<ChatItem?>
    val allFileDescriptions: List<FileDescription?>?
}
