package im.threads;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.mfms.android.push_lite.PushBroadcastReceiver;
import com.mfms.android.push_lite.PushController;
import com.mfms.android.push_lite.PushServerIntentService;
import com.mfms.android.push_lite.repo.push.remote.model.PushMessage;

import java.io.File;

import im.threads.activities.ChatActivity;
import im.threads.controllers.ChatController;
import im.threads.database.DatabaseHolder;
import im.threads.formatters.OutgoingMessageCreator;
import im.threads.internal.Config;
import im.threads.model.FileDescription;
import im.threads.model.UpcomingUserMessage;
import im.threads.utils.PrefUtils;
import im.threads.utils.Transport;

public final class ThreadsLib {

    private static ThreadsLib instance;

    public static void init(ConfigBuilder configBuilder) {
        if (instance != null) {
            throw new IllegalStateException("ThreadsLib has already been initialized");
        }
        Config.instance = configBuilder.build();
        instance = new ThreadsLib();
        PushController.getInstance(Config.instance.context).init();
        if (Config.instance.unreadMessagesCountListener != null) {
            DatabaseHolder.getInstance(Config.instance.context)
                    .getUnreadMessagesCount(false, Config.instance.unreadMessagesCountListener);
        }
    }

    public static ThreadsLib getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ThreadsLib should be initialized first with ThreadsLib.init()");
        }
        return instance;
    }

    private ThreadsLib() {
    }

    // send CLIENT_OFFLINE message
    public void logoutClient(final String clientId) {
        if (!TextUtils.isEmpty(clientId)) {
            Transport.sendMessageMFMSAsync(OutgoingMessageCreator.createMessageClientOffline(clientId), true, null, null);
        }
    }

    /**
     * Метод для отправки произвольного сообщения от имени клиента
     *
     * @return true, если удалось добавить сообщение в очередь отправки, иначе false
     */
    public boolean sendMessage(@Nullable String message, @Nullable File file) {
        if (PrefUtils.isClientIdNotEmpty()) {
            FileDescription fileDescription = null;
            if (file != null) {
                fileDescription = new FileDescription(Config.instance.context.getString(R.string.threads_I),
                        file.getAbsolutePath(),
                        file.length(),
                        System.currentTimeMillis());
            }
            UpcomingUserMessage msg = new UpcomingUserMessage(fileDescription, null, message, false);
            ChatController.getInstance(Config.instance.context).onUserInput(msg);
            return true;
        }
        return false;
    }

    public static final class ConfigBuilder {
        @NonNull
        private Context context;
        @NonNull
        private PendingIntentCreator pendingIntentCreator = (context1, appMarker) -> {
            final Intent i = new Intent(context1, ChatActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            return PendingIntent.getActivity(context1, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        };
        @Nullable
        private ShortPushListener shortPushListener;
        @Nullable
        private FullPushListener fullPushListener;
        @Nullable
        private UnreadMessagesCountListener unreadMessagesCountListener;

        public ConfigBuilder(@NonNull Context context) {
            this.context = context;
        }

        public ConfigBuilder pendingIntentCreator(@NonNull PendingIntentCreator pendingIntentCreator) {
            this.pendingIntentCreator = pendingIntentCreator;
            return this;
        }

        public ConfigBuilder shortPushListener(ShortPushListener shortPushListener) {
            this.shortPushListener = shortPushListener;
            return this;
        }

        public ConfigBuilder fullPushListener(FullPushListener fullPushListener) {
            this.fullPushListener = fullPushListener;
            return this;
        }

        public ConfigBuilder unreadMessagesCountListener(UnreadMessagesCountListener unreadMessagesCountListener) {
            this.unreadMessagesCountListener = unreadMessagesCountListener;
            return this;
        }

        private Config build() {
            return new Config(
                    context,
                    pendingIntentCreator,
                    shortPushListener,
                    fullPushListener,
                    unreadMessagesCountListener
            );
        }
    }

    public interface PendingIntentCreator {
        PendingIntent create(Context context, String appMarker);
    }

    public interface UnreadMessagesCountListener {
        void onUnreadMessagesCountChanged(int count);
    }

    /**
     * Оповещает о приходе короткого Push-уведомления.
     * Не срабатывает при опознанных системных Push-уведомлениях
     */
    public interface ShortPushListener {
        void onNewShortPushNotification(PushBroadcastReceiver pushBroadcastReceiver, Context context, String s, Bundle bundle);
    }

    /**
     * Оповещает о приходе полного Push-уведомления.
     * Не срабатывает, если удалось определить, что это уведомления для библиотеки чата.
     */
    public interface FullPushListener {
        void onNewFullPushNotification(PushServerIntentService pushServerIntentService, PushMessage pushMessage);
    }
}
