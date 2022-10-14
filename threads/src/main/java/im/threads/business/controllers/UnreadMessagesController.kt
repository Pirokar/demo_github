package im.threads.business.controllers

import im.threads.business.secureDatabase.DatabaseHolder
import im.threads.business.serviceLocator.core.inject
import im.threads.business.utils.preferences.PrefUtilsBase.unreadPushCount
import io.reactivex.processors.BehaviorProcessor

enum class UnreadMessagesController {
    INSTANCE;

    val unreadMessagesPublishProcessor = BehaviorProcessor.create<Int>()
    private val database: DatabaseHolder by inject()

    fun incrementUnreadPush() {
        unreadPushCount++
        refreshUnreadMessagesCount()
    }

    fun clearUnreadPush() {
        unreadPushCount = 0
        refreshUnreadMessagesCount()
    }

    /**
     * Оповещает об изменении количества непрочитанных сообщений.
     * Срабатывает при показе пуш уведомления в Статус Баре и
     * при прочтении сообщений.
     * Все места, где срабатывает прочтение сообщений, можно найти по
     * NotificationService.BROADCAST_ALL_MESSAGES_WERE_READ.
     * Данный тип сообщения отправляется в Сервис пуш уведомлений при прочтении сообщений.
     */
    fun refreshUnreadMessagesCount() {
        unreadMessagesPublishProcessor.onNext(unreadMessages)
    }

    val unreadMessages: Int
        get() = database.getUnreadMessagesCount() + unreadPushCount
}
