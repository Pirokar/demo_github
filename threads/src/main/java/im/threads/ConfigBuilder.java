package im.threads;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import im.threads.internal.Config;
import im.threads.view.ChatActivity;

public final class ConfigBuilder {
    @NonNull
    private Context context;
    @NonNull
    private ThreadsLib.PendingIntentCreator pendingIntentCreator = (context1, appMarker) -> {
        final Intent i = new Intent(context1, ChatActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context1, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
    };
    @Nullable
    private ThreadsLib.UnreadMessagesCountListener unreadMessagesCountListener = null;

    private boolean isDebugLoggingEnabled = false;

    private int historyLoadingCount = 50;

    private int surveyCompletionDelay = 2000;

    public ConfigBuilder(@NonNull Context context) {
        this.context = context;
    }

    public ConfigBuilder pendingIntentCreator(@NonNull ThreadsLib.PendingIntentCreator pendingIntentCreator) {
        this.pendingIntentCreator = pendingIntentCreator;
        return this;
    }

    public ConfigBuilder unreadMessagesCountListener(ThreadsLib.UnreadMessagesCountListener unreadMessagesCountListener) {
        this.unreadMessagesCountListener = unreadMessagesCountListener;
        return this;
    }

    public ConfigBuilder isDebugLoggingEnabled(boolean isDebugLoggingEnabled) {
        this.isDebugLoggingEnabled = isDebugLoggingEnabled;
        return this;
    }

    public ConfigBuilder setHistoryLoadingCount(final int historyLoadingCount) {
        this.historyLoadingCount = historyLoadingCount;
        return this;
    }

    public ConfigBuilder surveyCompletionDelay(final int  surveyCompletionDelay) {
        this.surveyCompletionDelay = surveyCompletionDelay;
        return this;
    }

    final Config build() {
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
