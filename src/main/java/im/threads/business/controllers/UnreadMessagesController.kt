package im.threads.business.controllers

import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.secureDatabase.DatabaseHolder
import im.threads.business.serviceLocator.core.inject
import io.reactivex.processors.BehaviorProcessor

enum class UnreadMessagesController {
    INSTANCE;

    private val database: DatabaseHolder by inject()
    private val preferences: Preferences by inject()

    val unreadMessagesPublishProcessor = BehaviorProcessor.create<Int>()
    private var unreadPushCount: Int
        get() = preferences.get(PreferencesCoreKeys.UNREAD_PUSH_COUNT) ?: 0
        set(value) { preferences.save(PreferencesCoreKeys.UNREAD_PUSH_COUNT, value) }

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
