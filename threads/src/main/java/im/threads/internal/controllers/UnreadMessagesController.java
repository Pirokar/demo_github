package im.threads.internal.controllers;

import im.threads.internal.database.DatabaseHolder;
import io.reactivex.processors.PublishProcessor;

public enum  UnreadMessagesController {

    INSTANCE;

    private PublishProcessor<Integer> unreadMessagesPublishProcessor = PublishProcessor.create();

    private int unreadPush = 0;

    public PublishProcessor<Integer> getUnreadMessagesPublishProcessor() {
        return unreadMessagesPublishProcessor;
    }

    public void incrementUnreadPush() {
        unreadPush++;
        refreshUnreadMessagesCount();
    }

    public void clearUnreadPush() {
        unreadPush = 0;
        refreshUnreadMessagesCount();
    }

    /**
     * Оповещает об изменении количества непрочитанных сообщений.
     * Срабатывает при показе пуш уведомления в Статус Баре и
     * при прочтении сообщений.
     * Все места, где срабатывает прочтение сообщений, можно найти по
     * NotificationService.BROADCAST_ALL_MESSAGES_WERE_READ.
     * Данный тип сообщения отправляется в Сервис пуш уведомлений при прочтении сообщений.
     * <p>
     * Можно было бы поместить оповещение в точку прихода NotificationService.BROADCAST_ALL_MESSAGES_WERE_READ,
     * но иногда в этот момент в сообщения еще не помечены, как прочитанные.
     */
    public void refreshUnreadMessagesCount() {
        unreadMessagesPublishProcessor.onNext(getUnreadMessages());
    }

    public int getUnreadMessages() {
        return DatabaseHolder.getInstance().getUnreadMessagesCount() + unreadPush;
    }

}
