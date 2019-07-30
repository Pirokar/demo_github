package im.threads;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.mfms.android.push_lite.PushController;

import java.io.File;

import im.threads.activities.ChatActivity;
import im.threads.controllers.ChatController;
import im.threads.database.DatabaseHolder;
import im.threads.formatters.OutgoingMessageCreator;
import im.threads.internal.Config;
import im.threads.internal.ThreadsLogger;
import im.threads.model.ChatStyle;
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

    public void initUser(UserInfo userInfo) {
        PrefUtils.setAppMarker(userInfo.appMarker);
        PrefUtils.setNewClientId(userInfo.clientId);
        PrefUtils.setClientIdSignature(userInfo.clientIdSignature);
        PrefUtils.setUserName(userInfo.userName);
        PrefUtils.setData(userInfo.data);
        PrefUtils.setClientIdEncrypted(userInfo.clientIdEncrypted);
    }

    public void applyChatStyle(ChatStyle.Builder builder) {
        ChatStyle.applyChatStyle(builder);
    }

    /**
     * Used to stop receiving messages for user with provided clientId
     */
    public void logoutClient(@NonNull final String clientId) {
        if (!TextUtils.isEmpty(clientId)) {
            Transport.sendMessageMFMSAsync(OutgoingMessageCreator.createMessageClientOffline(clientId), true, null, null);
        } else {
            ThreadsLogger.i(getClass().getSimpleName(), "clientId must not be empty");
        }
    }

    public void reloadHistory() {
        ChatController.getInstance(Config.instance.context).loadHistory();
    }

    /**
     * Used to post messages to chat as if written by client
     *
     * @return true, if message was successfully added to messaging queue, otherwise false
     */
    public boolean sendMessage(@Nullable String message, @Nullable File file) {
        ChatController chatController = ChatController.getInstance(Config.instance.context);
        if (PrefUtils.isClientIdNotEmpty()) {
            FileDescription fileDescription = null;
            if (file != null) {
                fileDescription = new FileDescription(Config.instance.context.getString(R.string.threads_I),
                        file.getAbsolutePath(),
                        file.length(),
                        System.currentTimeMillis());
            }
            UpcomingUserMessage msg = new UpcomingUserMessage(fileDescription, null, message, false);
            chatController.onUserInput(msg);
            return true;
        } else {
            ThreadsLogger.i(getClass().getSimpleName(), "You might need to initialize user first with ThreadsLib.userInfo()");
            return false;
        }
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
        private UnreadMessagesCountListener unreadMessagesCountListener = null;

        private boolean isDebugLoggingEnabled = false;

        private int historyLoadingCount = 50;

        private int surveyCompletionDelay = 2000;

        public ConfigBuilder(@NonNull Context context) {
            this.context = context;
        }

        public ConfigBuilder pendingIntentCreator(@NonNull PendingIntentCreator pendingIntentCreator) {
            this.pendingIntentCreator = pendingIntentCreator;
            return this;
        }

        public ConfigBuilder unreadMessagesCountListener(UnreadMessagesCountListener unreadMessagesCountListener) {
            this.unreadMessagesCountListener = unreadMessagesCountListener;
            return this;
        }

        public ConfigBuilder isDebugLoggingEnabled(boolean isDebugLoggingEnabled) {
            this.isDebugLoggingEnabled = isDebugLoggingEnabled;
            return this;
        }

        public ConfigBuilder surveyCompletionDelay(final int  surveyCompletionDelay) {
            this.surveyCompletionDelay = surveyCompletionDelay;
            return this;
        }

        public ConfigBuilder setHistoryLoadingCount(final int historyLoadingCount) {
            this.historyLoadingCount = historyLoadingCount;
            return this;
        }

        private Config build() {
            return new Config(
                    context,
                    pendingIntentCreator,
                    unreadMessagesCountListener,
                    isDebugLoggingEnabled,
                    historyLoadingCount,
                    surveyCompletionDelay
            );
        }
    }

    public static final class UserInfo {
        @NonNull
        private String clientId;
        @Nullable
        private String clientIdSignature = null;
        @Nullable
        private String userName = null;
        @Nullable
        private String data = null;
        @Nullable
        private String appMarker = null;

        /**
         * true if client id is encrypted
         */
        private boolean clientIdEncrypted = false;

        public UserInfo(@NonNull String clientId) {
            if (TextUtils.isEmpty(clientId)) {
                throw new IllegalArgumentException("clientId must not be empty");
            }
            this.clientId = clientId;
        }

        public UserInfo setClientIdSignature(String clientIdSignature) {
            this.clientIdSignature = clientIdSignature;
            return this;
        }

        public UserInfo setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        /**
         * Any additional information can be provided in data string, i.e. "{balance:"1000.00", fio:"Vasya Pupkin"}"
         */
        public UserInfo setData(String data) {
            this.data = data;
            return this;
        }

        public UserInfo setAppMarker(String appMarker) {
            this.appMarker = appMarker;
            return this;
        }

        public UserInfo setClientIdEncrypted(boolean clientIdEncrypted) {
            this.clientIdEncrypted = clientIdEncrypted;
            return this;
        }
    }

    public interface PendingIntentCreator {
        PendingIntent create(Context context, String appMarker);
    }

    public interface UnreadMessagesCountListener {
        void onUnreadMessagesCountChanged(int count);
    }
}
