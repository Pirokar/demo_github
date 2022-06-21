package im.threads;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;

import im.threads.config.RequestConfig;
import im.threads.internal.Config;
import im.threads.view.ChatActivity;
import okhttp3.Interceptor;

public final class ConfigBuilder {
    @NonNull
    Context context;
    @NonNull
    private ThreadsLib.PendingIntentCreator pendingIntentCreator = (context1, appMarker) -> {
        final Intent i = new Intent(context1, ChatActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_CANCEL_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getActivity(context1, 0, i, flags);
    };
    @Nullable
    private ThreadsLib.UnreadMessagesCountListener unreadMessagesCountListener = null;

    private boolean isDebugLoggingEnabled = true;

    private int historyLoadingCount = 50;

    private int surveyCompletionDelay = 2000;

    @Nullable
    private String serverBaseUrl = null;
    @Nullable
    private ConfigBuilder.TransportType transportType = null;
    @Nullable
    private String threadsGateUrl = null;
    @Nullable
    private String threadsGateProviderUid = null;
    @Nullable
    private String threadsGateHCMProviderUid = null;
    @Nullable
    private Interceptor networkInterceptor = null;

    private RequestConfig requestConfig = new RequestConfig();
    private List<Integer> certificateRawResIds = Collections.emptyList();

    public ConfigBuilder(@NonNull Context context) {
        this.context = context;
    }

    public ConfigBuilder serverBaseUrl(String serverBaseUrl) {
        this.serverBaseUrl = serverBaseUrl;
        return this;
    }

    public ConfigBuilder transportType(ConfigBuilder.TransportType transportType) {
        this.transportType = transportType;
        return this;
    }

    public ConfigBuilder threadsGateUrl(String threadsGateUrl) {
        this.threadsGateUrl = threadsGateUrl;
        return this;
    }

    public ConfigBuilder threadsGateProviderUid(String threadsGateProviderUid) {
        this.threadsGateProviderUid = threadsGateProviderUid;
        return this;
    }

    public ConfigBuilder threadsGateHCMProviderUid(@Nullable String threadsGateHCMProviderUid) {
        this.threadsGateHCMProviderUid = threadsGateHCMProviderUid;
        return this;
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

    public ConfigBuilder requestConfig(final RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
        return this;
    }

    public ConfigBuilder certificateRawResIds(final List<Integer> certificateRawResIds) {
        this.certificateRawResIds = certificateRawResIds;
        return this;
    }

    public ConfigBuilder networkInterceptor(Interceptor interceptor) {
        this.networkInterceptor = interceptor;
        return this;
    }

    Config build() {
        return new Config(
                context,
                serverBaseUrl,
                transportType,
                threadsGateUrl,
                threadsGateProviderUid,
                threadsGateHCMProviderUid,
                pendingIntentCreator,
                unreadMessagesCountListener,
                networkInterceptor,
                isDebugLoggingEnabled,
                historyLoadingCount,
                surveyCompletionDelay,
                requestConfig,
                certificateRawResIds
        );
    }

    public enum TransportType {
        @Deprecated
        MFMS_PUSH,
        THREADS_GATE;

        @NonNull
        public static TransportType fromString(@NonNull String name) {
            return valueOf(name);
        }
    }
}
