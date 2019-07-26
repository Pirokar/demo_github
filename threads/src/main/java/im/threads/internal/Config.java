package im.threads.internal;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import im.threads.ThreadsLib;

public class Config {

    public static Config instance;

    @NonNull
    public final Context context;
    @NonNull
    public final ThreadsLib.PendingIntentCreator pendingIntentCreator;
    @Nullable
    public final ThreadsLib.UnreadMessagesCountListener unreadMessagesCountListener;

    public final boolean isDebugLoggingEnabled;
    /**
     * set history loading count
     */
    public final int historyLoadingCount;

    public final int surveyCompletionDelay;

    public Config(@NonNull Context context,
                  @NonNull ThreadsLib.PendingIntentCreator pendingIntentCreator,
                  @Nullable ThreadsLib.UnreadMessagesCountListener unreadMessagesCountListener,
                  boolean isDebugLoggingEnabled,
                  int historyLoadingCount,
                  int surveyCompletionDelay) {
        this.context = context.getApplicationContext();
        this.pendingIntentCreator = pendingIntentCreator;
        this.unreadMessagesCountListener = unreadMessagesCountListener;
        this.isDebugLoggingEnabled = isDebugLoggingEnabled;
        this.historyLoadingCount = historyLoadingCount;
        this.surveyCompletionDelay = surveyCompletionDelay;
    }
}
