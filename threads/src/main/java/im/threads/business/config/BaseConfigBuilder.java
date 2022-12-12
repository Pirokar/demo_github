package im.threads.business.config;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.Collections;
import java.util.List;

import im.threads.business.core.UnreadMessagesCountListener;
import im.threads.business.logger.LoggerConfig;
import im.threads.business.rest.config.RequestConfig;
import okhttp3.Interceptor;

public class BaseConfigBuilder {
    @NonNull
    public Context context;

    @Nullable
    protected UnreadMessagesCountListener unreadMessagesCountListener = null;

    protected boolean isDebugLoggingEnabled = true;

    protected int historyLoadingCount = 50;

    protected int surveyCompletionDelay = 2000;

    @Nullable
    protected String serverBaseUrl = null;
    @Nullable
    protected String datastoreUrl = null;
    @Nullable
    protected String threadsGateUrl = null;
    @Nullable
    protected String threadsGateProviderUid = null;
    @Nullable
    protected Interceptor networkInterceptor = null;
    @Nullable
    protected Boolean isNewChatCenterApi = false;
    @Nullable
    protected LoggerConfig loggerConfig = null;

    protected RequestConfig requestConfig = new RequestConfig();
    protected List<Integer> certificateRawResIds = Collections.emptyList();
    protected Boolean isSslPinningDisabled = false;

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected int notificationImportance = NotificationManager.IMPORTANCE_DEFAULT;

    public BaseConfigBuilder(@NonNull Context context) {
        this.context = context;
    }

    public BaseConfigBuilder serverBaseUrl(String serverBaseUrl) {
        this.serverBaseUrl = serverBaseUrl;
        return this;
    }

    public BaseConfigBuilder datastoreUrl(String datastoreUrl) {
        this.datastoreUrl = datastoreUrl;
        return this;
    }

    public BaseConfigBuilder threadsGateUrl(String threadsGateUrl) {
        this.threadsGateUrl = threadsGateUrl;
        return this;
    }

    public BaseConfigBuilder threadsGateProviderUid(String threadsGateProviderUid) {
        this.threadsGateProviderUid = threadsGateProviderUid;
        return this;
    }

    public BaseConfigBuilder unreadMessagesCountListener(UnreadMessagesCountListener unreadMessagesCountListener) {
        this.unreadMessagesCountListener = unreadMessagesCountListener;
        return this;
    }

    public BaseConfigBuilder isDebugLoggingEnabled(boolean isDebugLoggingEnabled) {
        this.isDebugLoggingEnabled = isDebugLoggingEnabled;
        return this;
    }

    public BaseConfigBuilder historyLoadingCount(final int historyLoadingCount) {
        this.historyLoadingCount = historyLoadingCount;
        return this;
    }

    public BaseConfigBuilder surveyCompletionDelay(final int surveyCompletionDelay) {
        this.surveyCompletionDelay = surveyCompletionDelay;
        return this;
    }

    public BaseConfigBuilder requestConfig(final RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
        return this;
    }

    public BaseConfigBuilder certificateRawResIds(final List<Integer> certificateRawResIds) {
        this.certificateRawResIds = certificateRawResIds;
        return this;
    }

    /**
     * Выключает SSL pinning, даже если передан сертификат
     */
    public BaseConfigBuilder disableSSLPinning() {
        this.isSslPinningDisabled = true;
        return this;
    }

    public BaseConfigBuilder networkInterceptor(Interceptor interceptor) {
        this.networkInterceptor = interceptor;
        return this;
    }

    public BaseConfigBuilder setNewChatCenterApi() {
        this.isNewChatCenterApi = true;
        return this;
    }

    public BaseConfigBuilder enableLogging(LoggerConfig config) {
        this.loggerConfig = config;
        return this;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BaseConfigBuilder setNotificationImportance(int importance) {
        this.notificationImportance = importance;
        return this;
    }

    protected int getNotificationLevel() {
        int notificationLevel = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationLevel = notificationImportance;
        }

        return notificationLevel;
    }

    public BaseConfig build() {
        return new BaseConfig(
                context,
                serverBaseUrl,
                datastoreUrl,
                threadsGateUrl,
                threadsGateProviderUid,
                isNewChatCenterApi,
                loggerConfig,
                unreadMessagesCountListener,
                networkInterceptor,
                isDebugLoggingEnabled,
                historyLoadingCount,
                surveyCompletionDelay,
                requestConfig,
                isSslPinningDisabled,
                getNotificationLevel(),
                certificateRawResIds
        );
    }
}
