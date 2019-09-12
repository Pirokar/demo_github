package im.threads.internal;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import im.threads.ChatStyle;
import im.threads.ThreadsLib;
import im.threads.internal.transport.Transport;
import im.threads.internal.utils.PrefUtils;

public final class Config {

    public static Config instance;

    @NonNull
    public final Context context;

    private volatile ChatStyle chatStyle = null;

    @NonNull
    public final ThreadsLib.PendingIntentCreator pendingIntentCreator;
    @Nullable
    public final ThreadsLib.UnreadMessagesCountListener unreadMessagesCountListener;
    @NonNull
    public final Transport transport;

    public final boolean isDebugLoggingEnabled;
    /**
     * set history loading count
     */
    public final int historyLoadingCount;

    public final int surveyCompletionDelay;

    public final Gson gson = new Gson();

    public Config(@NonNull Context context,
                  @NonNull ThreadsLib.PendingIntentCreator pendingIntentCreator,
                  @Nullable ThreadsLib.UnreadMessagesCountListener unreadMessagesCountListener,
                  boolean isDebugLoggingEnabled,
                  int historyLoadingCount,
                  int surveyCompletionDelay,
                  @NonNull Transport transport) {
        this.context = context.getApplicationContext();
        this.pendingIntentCreator = pendingIntentCreator;
        this.unreadMessagesCountListener = unreadMessagesCountListener;
        this.isDebugLoggingEnabled = isDebugLoggingEnabled;
        this.historyLoadingCount = historyLoadingCount;
        this.surveyCompletionDelay = surveyCompletionDelay;
        this.transport = transport;
    }

    public void applyChatStyle(ChatStyle chatStyle) {
        this.chatStyle = chatStyle;
        PrefUtils.setIncomingStyle(chatStyle);
    }

    @NonNull
    public ChatStyle getChatStyle() {
        ChatStyle localInstance = chatStyle;
        if (localInstance == null) {
            synchronized (ChatStyle.class) {
                localInstance = chatStyle;
                if (localInstance == null) {
                    localInstance = PrefUtils.getIncomingStyle();
                    if (localInstance == null) {
                        localInstance = new ChatStyle();
                    }
                    chatStyle = localInstance;
                }
            }
        }
        return localInstance;
    }

    public enum TransportType {
        MFMS_PUSH,
        THREADS_GATE
    }
}
