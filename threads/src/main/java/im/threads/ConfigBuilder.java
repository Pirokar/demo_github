package im.threads;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import im.threads.internal.Config;
import im.threads.internal.transport.Transport;
import im.threads.internal.transport.mfms_push.MFMSPushTransport;
import im.threads.internal.transport.threads_gate.ThreadsGateTransport;
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

    private Config.TransportType transportType = Config.TransportType.MFMS_PUSH;

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

    public ConfigBuilder historyLoadingCount(final int historyLoadingCount) {
        this.historyLoadingCount = historyLoadingCount;
        return this;
    }

    public ConfigBuilder surveyCompletionDelay(final int surveyCompletionDelay) {
        this.surveyCompletionDelay = surveyCompletionDelay;
        return this;
    }

    public ConfigBuilder transportType(final Config.TransportType transportType) {
        this.transportType = transportType;
        return this;
    }

    final Config build() {
        Transport transport;
        switch (transportType) {
            case THREADS_GATE:
                transport = new ThreadsGateTransport();
                break;
            case MFMS_PUSH:
            default:
                transport = new MFMSPushTransport();
                break;
        }
        return new Config(
                context,
                pendingIntentCreator,
                unreadMessagesCountListener,
                isDebugLoggingEnabled,
                historyLoadingCount,
                surveyCompletionDelay,
                transport
        );
    }
}
