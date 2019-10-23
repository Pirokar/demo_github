package im.threads;

import android.app.PendingIntent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.mfms.android.push_lite.PushController;

import java.io.File;

import im.threads.internal.Config;
import im.threads.internal.controllers.ChatController;
import im.threads.internal.database.DatabaseHolder;
import im.threads.internal.formatters.OutgoingMessageCreator;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.UpcomingUserMessage;
import im.threads.internal.utils.PrefUtils;
import im.threads.internal.utils.ThreadsLogger;
import im.threads.internal.utils.Transport;

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
        PushController.getInstance(Config.instance.context).init();
        if (Config.instance.unreadMessagesCountListener != null) {
            DatabaseHolder.getInstance()
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

    public interface PendingIntentCreator {
        PendingIntent create(Context context, String appMarker);
    }

    public interface UnreadMessagesCountListener {
        void onUnreadMessagesCountChanged(int count);
    }
}