package im.threads;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;

import im.threads.business.config.RequestConfig;
import im.threads.business.logger.LoggerConfig;
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
    private String datastoreUrl = null;
    @Nullable
    private String threadsGateUrl = null;
    @Nullable
    private String threadsGateProviderUid = null;
    @Nullable
    private String threadsGateHCMProviderUid = null;
    @Nullable
    private Interceptor networkInterceptor = null;
    @Nullable
    private Boolean isNewChatCenterApi = false;
    @Nullable
    private LoggerConfig loggerConfig = null;
    @Nullable
    private ChatStyle chatStyle = null;

    private RequestConfig requestConfig = new RequestConfig();
    private List<Integer> certificateRawResIds = Collections.emptyList();

    public ConfigBuilder(@NonNull Context context) {
        this.context = context;
    }

    public ConfigBuilder serverBaseUrl(String serverBaseUrl) {
        this.serverBaseUrl = serverBaseUrl;
        return this;
    }

    public ConfigBuilder datastoreUrl(String datastoreUrl) {
        this.datastoreUrl = datastoreUrl;
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

    public ConfigBuilder setNewChatCenterApi() {
        this.isNewChatCenterApi = true;
        return this;
    }

    public ConfigBuilder applyChatStyle(ChatStyle chatStyle) {
        this.chatStyle = chatStyle;
        return this;
    }

    public ConfigBuilder enableLogging(LoggerConfig config) {
        this.loggerConfig = config;
        return this;
    }

    Config build() {
        return new Config(
                context,
                serverBaseUrl,
                datastoreUrl,
                threadsGateUrl,
                threadsGateProviderUid,
                threadsGateHCMProviderUid,
                isNewChatCenterApi,
                loggerConfig,
                pendingIntentCreator,
                unreadMessagesCountListener,
                networkInterceptor,
                chatStyle,
                isDebugLoggingEnabled,
                historyLoadingCount,
                surveyCompletionDelay,
                requestConfig,
                certificateRawResIds
        );
    }
}
