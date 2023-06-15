package im.threads.business.config

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import im.threads.business.core.UnreadMessagesCountListener
import im.threads.business.logger.LoggerConfig
import im.threads.business.rest.config.RequestConfig
import okhttp3.Interceptor

open class BaseConfigBuilder(var context: Context) {
    protected var unreadMessagesCountListener: UnreadMessagesCountListener? = null
    protected var isDebugLoggingEnabled = false
    protected var historyLoadingCount = 50
    protected var surveyCompletionDelay = 2000
    protected var serverBaseUrl: String? = null
    protected var datastoreUrl: String? = null
    protected var threadsGateUrl: String? = null
    protected var threadsGateProviderUid: String? = null
    protected var networkInterceptor: Interceptor? = null
    protected var isNewChatCenterApi: Boolean? = false
    protected var loggerConfig: LoggerConfig? = null
    protected var requestConfig = RequestConfig()
    protected var trustedSSLCertificates = emptyList<Int>()
    protected var allowUntrustedSSLCertificate = false

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected var notificationImportance = NotificationManager.IMPORTANCE_DEFAULT
    open fun serverBaseUrl(serverBaseUrl: String?): BaseConfigBuilder? {
        this.serverBaseUrl = serverBaseUrl
        return this
    }

    open fun datastoreUrl(datastoreUrl: String?): BaseConfigBuilder? {
        this.datastoreUrl = datastoreUrl
        return this
    }

    open fun threadsGateUrl(threadsGateUrl: String?): BaseConfigBuilder? {
        this.threadsGateUrl = threadsGateUrl
        return this
    }

    open fun threadsGateProviderUid(threadsGateProviderUid: String?): BaseConfigBuilder? {
        this.threadsGateProviderUid = threadsGateProviderUid
        return this
    }

    open fun unreadMessagesCountListener(unreadMessagesCountListener: UnreadMessagesCountListener?): BaseConfigBuilder? {
        this.unreadMessagesCountListener = unreadMessagesCountListener
        return this
    }

    open fun isDebugLoggingEnabled(isDebugLoggingEnabled: Boolean): BaseConfigBuilder? {
        this.isDebugLoggingEnabled = isDebugLoggingEnabled
        return this
    }

    open fun historyLoadingCount(historyLoadingCount: Int): BaseConfigBuilder? {
        this.historyLoadingCount = historyLoadingCount
        return this
    }

    open fun surveyCompletionDelay(surveyCompletionDelay: Int): BaseConfigBuilder? {
        this.surveyCompletionDelay = surveyCompletionDelay
        return this
    }

    open fun requestConfig(requestConfig: RequestConfig): BaseConfigBuilder? {
        this.requestConfig = requestConfig
        return this
    }

    open fun trustedSSLCertificates(trustedSSLCertificates: List<Int>?): BaseConfigBuilder? {
        if (trustedSSLCertificates.isNullOrEmpty()) {
            this.trustedSSLCertificates = emptyList()
        } else {
            this.trustedSSLCertificates = trustedSSLCertificates
        }
        return this
    }

    open fun allowUntrustedSSLCertificates(allowUntrustedSSLCertificate: Boolean): BaseConfigBuilder? {
        this.allowUntrustedSSLCertificate = allowUntrustedSSLCertificate
        return this
    }

    open fun networkInterceptor(interceptor: Interceptor?): BaseConfigBuilder? {
        networkInterceptor = interceptor
        return this
    }

    open fun setNewChatCenterApi(): BaseConfigBuilder? {
        isNewChatCenterApi = true
        return this
    }

    open fun enableLogging(config: LoggerConfig?): BaseConfigBuilder? {
        loggerConfig = config
        return this
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    open fun setNotificationImportance(importance: Int): BaseConfigBuilder? {
        notificationImportance = importance
        return this
    }

    protected val notificationLevel: Int
        get() {
            var notificationLevel = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                notificationLevel = notificationImportance
            }
            return notificationLevel
        }

    open fun build(): BaseConfig {
        return BaseConfig(
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
            notificationLevel,
            trustedSSLCertificates,
            allowUntrustedSSLCertificate
        )
    }
}
