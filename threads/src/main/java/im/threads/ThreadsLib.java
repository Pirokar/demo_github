package im.threads;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.ObjectsCompat;
import im.threads.internal.Config;
import im.threads.internal.controllers.ChatController;
import im.threads.internal.controllers.UnreadMessagesController;
import im.threads.internal.helpers.FileProviderHelper;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.UpcomingUserMessage;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.utils.PrefUtils;
import im.threads.internal.utils.ThreadsLogger;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;

public final class ThreadsLib {

    private static final String TAG = ThreadsLib.class.getSimpleName();

    private static ThreadsLib instance;

    private ThreadsLib() {
    }

    public static String getLibVersion() {
        return BuildConfig.VERSION_NAME;
    }

    public static void init(ConfigBuilder configBuilder) {
        if (instance != null) {
            throw new IllegalStateException("ThreadsLib has already been initialized");
        }
        Config.instance = configBuilder.build();
        instance = new ThreadsLib();
        PrefUtils.migrateToSeparateStorageIfNeeded();
        Config.instance.transport.init();
        if (Config.instance.unreadMessagesCountListener != null) {
            Config.instance.unreadMessagesCountListener.onUnreadMessagesCountChanged(UnreadMessagesController.INSTANCE.getUnreadMessages());
            UnreadMessagesController.INSTANCE.getUnreadMessagesPublishProcessor()
                    .distinctUntilChanged()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(count -> Config.instance.unreadMessagesCountListener.onUnreadMessagesCountChanged(count));
        }
        ChatController.getInstance();
        Consumer<? super Throwable> errorHandler = RxJavaPlugins.getErrorHandler();
        RxJavaPlugins.setErrorHandler(throwable -> {
            if (errorHandler != null) {
                errorHandler.accept(throwable);
            }
            if (throwable instanceof UndeliverableException) {
                throwable = throwable.getCause();
                if (throwable != null) {
                    ThreadsLogger.e(TAG, "global handler: ", throwable);
                }
                return;
            }
            Thread.currentThread().getUncaughtExceptionHandler()
                    .uncaughtException(Thread.currentThread(), throwable);
        });

    }

    public static ThreadsLib getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ThreadsLib should be initialized first with ThreadsLib.init()");
        }
        return instance;
    }

    public void initUser(UserInfoBuilder userInfoBuilder) {
        final String currentClientId = PrefUtils.getClientID();
        if (currentClientId != null && !ObjectsCompat.equals(currentClientId, userInfoBuilder.clientId)) {
            logoutClient(currentClientId);
        }
        PrefUtils.setAppMarker(userInfoBuilder.appMarker);
        PrefUtils.setNewClientId(userInfoBuilder.clientId);
        PrefUtils.setClientIdSignature(userInfoBuilder.clientIdSignature);
        PrefUtils.setUserName(userInfoBuilder.userName);
        PrefUtils.setData(userInfoBuilder.clientData);
        PrefUtils.setClientIdEncrypted(userInfoBuilder.clientIdEncrypted);
        ChatController.getInstance().sendInit();
        ChatController.getInstance().loadHistory();
    }

    public void applyChatStyle(ChatStyle chatStyle) {
        Config.instance.applyChatStyle(chatStyle);
    }

    /**
     * Used to stop receiving messages for user with provided clientId
     */
    public void logoutClient(@NonNull final String clientId) {
        if (!TextUtils.isEmpty(clientId)) {
            Config.instance.transport.sendClientOffline(clientId);
        } else {
            ThreadsLogger.i(getClass().getSimpleName(), "clientId must not be empty");
        }
    }

    /**
     * Used to post messages to chat as if written by client
     *
     * @return true, if message was successfully added to messaging queue, otherwise false
     */
    public boolean sendMessage(@Nullable String message, @Nullable File file) {
        Uri fileUri = file != null ? FileProviderHelper.getUriForFile(Config.instance.context, file) : null;
        return sendMessage(message, fileUri);
    }

    /**
     * Used to post messages to chat as if written by client
     *
     * @return true, if message was successfully added to messaging queue, otherwise false
     */
    public boolean sendMessage(@Nullable String message, @Nullable Uri fileUri) {
        ChatController chatController = ChatController.getInstance();
        if (PrefUtils.isClientIdNotEmpty()) {
            FileDescription fileDescription = null;
            if (fileUri != null) {
                fileDescription = new FileDescription(
                        Config.instance.context.getString(R.string.threads_I),
                        fileUri,
                        FileUtils.getFileSize(fileUri),
                        System.currentTimeMillis()
                );
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
