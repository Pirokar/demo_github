package im.threads;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.ObjectsCompat;

import java.io.File;
import java.util.Map;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import im.threads.internal.Config;
import im.threads.internal.chat_updates.ChatUpdateProcessor;
import im.threads.internal.controllers.ChatController;
import im.threads.internal.controllers.UnreadMessagesController;
import im.threads.internal.helpers.FileProviderHelper;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.UpcomingUserMessage;
import im.threads.internal.useractivity.LastUserActivityTimeCounter;
import im.threads.internal.useractivity.LastUserActivityTimeCounterSingletonProvider;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.utils.PrefUtils;
import im.threads.internal.utils.ThreadsLogger;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.processors.FlowableProcessor;

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
        if (Config.instance.unreadMessagesCountListener != null) {
            Config.instance.unreadMessagesCountListener.onUnreadMessagesCountChanged(UnreadMessagesController.INSTANCE.getUnreadMessages());
            UnreadMessagesController.INSTANCE.getUnreadMessagesPublishProcessor()
                    .distinctUntilChanged()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(count -> Config.instance.unreadMessagesCountListener.onUnreadMessagesCountChanged(count),
                            error -> ThreadsLogger.e(TAG, "init " + error.getMessage()));
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                AndroidAudioConverter.load(Config.instance.context, new ILoadCallback() {
                    @Override
                    public void onSuccess() {
                        ThreadsLogger.i(TAG, "AndroidAudioConverter was successfully loaded");
                    }

                    @Override
                    public void onFailure(Exception error) {
                        ThreadsLogger.e(TAG, "AndroidAudioConverter failed to load", error);
                    }
                });
            } catch (UnsatisfiedLinkError e) {
                ThreadsLogger.e(TAG, "AndroidAudioConverter failed to load (UnsatisfiedLinkError)", e);
            }
        }
        ChatController.getInstance();
        LastUserActivityTimeCounterSingletonProvider.INSTANCE.getLastUserActivityTimeCounter();
        if (RxJavaPlugins.getErrorHandler() == null) {
            RxJavaPlugins.setErrorHandler(throwable -> {
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
    }

    public static ThreadsLib getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ThreadsLib should be initialized first with ThreadsLib.init()");
        }
        return instance;
    }

    public void initUser(UserInfoBuilder userInfoBuilder) {
        if (!Config.instance.clientIdIgnoreEnabled) {
            final String currentClientId = PrefUtils.getClientID();
            if (currentClientId != null && !ObjectsCompat.equals(currentClientId, userInfoBuilder.clientId)) {
                logoutClient(currentClientId);
            }
        } else {
            // it will only affect GPB, every time they try to init user we will delete user related data
            ChatController.getInstance().cleanAll();
        }
        PrefUtils.setAppMarker(userInfoBuilder.appMarker);
        PrefUtils.setNewClientId(userInfoBuilder.clientId);
        PrefUtils.setAuthToken(userInfoBuilder.authToken);
        PrefUtils.setAuthSchema(userInfoBuilder.authSchema);
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
        if (!PrefUtils.isClientIdEmpty()) {
            FileDescription fileDescription = null;
            if (fileUri != null) {
                fileDescription = new FileDescription(
                        Config.instance.context.getString(R.string.threads_I),
                        fileUri,
                        FileUtils.getFileSize(fileUri),
                        System.currentTimeMillis()
                );
            }
            UpcomingUserMessage msg = new UpcomingUserMessage(fileDescription, null, null, message, false);
            chatController.onUserInput(msg);
            return true;
        } else {
            ThreadsLogger.i(getClass().getSimpleName(), "You might need to initialize user first with ThreadsLib.userInfo()");
            return false;
        }
    }

    /**
     * @return time in seconds since the last user activity
     */
    public long getSecondsSinceLastActivity() {
        LastUserActivityTimeCounter timeCounter = LastUserActivityTimeCounterSingletonProvider
                .INSTANCE.getLastUserActivityTimeCounter();
        return timeCounter.getSecondsSinceLastActivity();
    }

    public boolean isUserInitialized() {
        return !PrefUtils.isClientIdEmpty();
    }

    /**
     * @return FlowableProcessor that emits responses from WebSocket connection
     */
    public FlowableProcessor<Map<String, Object>> getSocketResponseMapProcessor() {
        return ChatUpdateProcessor.getInstance().getSocketResponseMapProcessor();
    }

    public interface PendingIntentCreator {
        PendingIntent create(Context context, String appMarker);
    }

    public interface UnreadMessagesCountListener {
        void onUnreadMessagesCountChanged(int count);
    }
}
