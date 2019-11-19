package im.threads;

import android.app.PendingIntent;
import android.content.Context;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import im.threads.internal.Config;
import im.threads.internal.controllers.ChatController;
import im.threads.internal.database.DatabaseHolder;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.UpcomingUserMessage;
import im.threads.internal.utils.PrefUtils;
import im.threads.internal.utils.ThreadsLogger;

public final class ThreadsLib {

    private static ThreadsLib instance;

    public static String getLibVersion() {
        return BuildConfig.VERSION_NAME;
    }

    public static void init(ConfigBuilder configBuilder) {
        if (instance != null) {
            throw new IllegalStateException("ThreadsLib has already been initialized");
        }
        Config.instance = configBuilder.build();
        instance = new ThreadsLib();
        Config.instance.transport.init();
        if (Config.instance.unreadMessagesCountListener != null) {
            DatabaseHolder.getInstance()
                    .getUnreadMessagesCount(false, Config.instance.unreadMessagesCountListener);
        }
        ChatController.getInstance();
    }

    public static ThreadsLib getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ThreadsLib should be initialized first with ThreadsLib.init()");
        }
        return instance;
    }

    private ThreadsLib() {
    }

    public void initUser(UserInfoBuilder userInfoBuilder) {
        PrefUtils.setAppMarker(userInfoBuilder.appMarker);
        PrefUtils.setNewClientId(userInfoBuilder.clientId);
        PrefUtils.setClientIdSignature(userInfoBuilder.clientIdSignature);
        PrefUtils.setUserName(userInfoBuilder.userName);
        PrefUtils.setData(userInfoBuilder.data);
        PrefUtils.setClientIdEncrypted(userInfoBuilder.clientIdEncrypted);
    }

    public void applyChatStyle(ChatStyle chatStyle) {
        Config.instance.applyChatStyle(chatStyle);
    }

    /**
     * Used to stop receiving messages for user with provided clientId
     */
    public void logoutClient(@NonNull final String clientId) {
        ChatController.getInstance().logoutClient(clientId);
    }

    public void reloadHistory() {
        ChatController.getInstance().loadHistory();
    }

    /**
     * Used to post messages to chat as if written by client
     *
     * @return true, if message was successfully added to messaging queue, otherwise false
     */
    public boolean sendMessage(@Nullable String message, @Nullable File file) {
        ChatController chatController = ChatController.getInstance();
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

    public interface PendingIntentCreator {
        PendingIntent create(Context context, String appMarker);
    }

    public interface UnreadMessagesCountListener {
        void onUnreadMessagesCountChanged(int count);
    }
}
