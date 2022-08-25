package im.threads.internal.controllers;

import im.threads.business.secureDatabase.DatabaseHolder;
import im.threads.business.utils.preferences.PrefUtilsBase;
import io.reactivex.processors.BehaviorProcessor;

public enum UnreadMessagesController {

    INSTANCE;

    private BehaviorProcessor<Integer> unreadMessagesPublishProcessor = BehaviorProcessor.create();

    public BehaviorProcessor<Integer> getUnreadMessagesPublishProcessor() {
        return unreadMessagesPublishProcessor;
    }

    public void incrementUnreadPush() {
        PrefUtilsBase.setUnreadPushCount(PrefUtilsBase.getUnreadPushCount() + 1);
        refreshUnreadMessagesCount();
    }

    public void clearUnreadPush() {
        PrefUtilsBase.setUnreadPushCount(0);
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
        return DatabaseHolder.getInstance().getUnreadMessagesCount() + PrefUtilsBase.getUnreadPushCount();
    }
}
