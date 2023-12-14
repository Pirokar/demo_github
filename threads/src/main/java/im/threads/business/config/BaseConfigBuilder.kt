package im.threads.business.config

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import im.threads.business.core.UnreadMessagesCountListener
import im.threads.business.logger.LoggerConfig
import im.threads.business.models.enums.ApiVersionEnum
import im.threads.business.rest.config.RequestConfig
import okhttp3.Interceptor

open class BaseConfigBuilder(var context: Context) {
    internal var unreadMessagesCountListener: UnreadMessagesCountListener? = null
    internal var isDebugLoggingEnabled = false
    internal var historyLoadingCount = 50
    internal var surveyCompletionDelay = 2000
    internal var serverBaseUrl: String? = null
    internal var datastoreUrl: String? = null
    internal var threadsGateUrl: String? = null
    internal var threadsGateProviderUid: String? = null
    internal var networkInterceptor: Interceptor? = null
    internal var isNewChatCenterApi: Boolean = false
    internal var loggerConfig: LoggerConfig? = null
    internal var requestConfig = RequestConfig()
    internal var trustedSSLCertificates = emptyList<Int>()
    internal var allowUntrustedSSLCertificate = false
    internal var keepSocketActive = false
    internal var apiVersion = ApiVersionEnum.defaultApiVersionEnum

    @RequiresApi(api = Build.VERSION_CODES.N)
    internal var notificationImportance = NotificationManager.IMPORTANCE_DEFAULT

    open fun serverBaseUrl(serverBaseUrl: String?): BaseConfigBuilder? {
        if (!serverBaseUrl.isNullOrBlank() && !serverBaseUrl.endsWith("/")) {
            this.serverBaseUrl = "$serverBaseUrl/"
        } else {
            this.serverBaseUrl = serverBaseUrl
        }
        return this
    }

    open fun keepSocketActive(keepSocketActive: Boolean): BaseConfigBuilder {
        this.keepSocketActive = keepSocketActive
        return this
    }

    open fun datastoreUrl(datastoreUrl: String?): BaseConfigBuilder? {
        if (!datastoreUrl.isNullOrBlank() && !datastoreUrl.endsWith("/")) {
            this.datastoreUrl = "$datastoreUrl/"
        } else {
            this.datastoreUrl = datastoreUrl
        }
        return this
    }

    open fun threadsGateUrl(threadsGateUrl: String?): BaseConfigBuilder? {
        if (!threadsGateUrl.isNullOrBlank() && threadsGateUrl.endsWith("/")) {
            this.threadsGateUrl = threadsGateUrl.dropLast(1)
        } else {
            this.threadsGateUrl = threadsGateUrl
        }
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

    /**
     * Устанавливает сертификаты для SSL пиннинга
     */
    open fun trustedSSLCertificates(trustedSSLCertificates: List<Int>?): BaseConfigBuilder? {
        if (trustedSSLCertificates.isNullOrEmpty()) {
            this.trustedSSLCertificates = emptyList()
        } else {
            this.trustedSSLCertificates = trustedSSLCertificates
        }
        return this
    }

    /**
     * Разрешает использовать недоверенные, в т.ч. самоподписанные сертификаты
     */
    open fun allowUntrustedSSLCertificates(allowUntrustedSSLCertificate: Boolean): BaseConfigBuilder? {
        this.allowUntrustedSSLCertificate = allowUntrustedSSLCertificate
        return this
    }

    /**
     * Добавляет интерсептор для OKHTTP в сетевых запросах
     */
    open fun networkInterceptor(interceptor: Interceptor?): BaseConfigBuilder? {
        networkInterceptor = interceptor
        return this
    }

    /**
     * Меняет роутинг запросов
     */
    open fun setNewChatCenterApi(): BaseConfigBuilder? {
        isNewChatCenterApi = true
        return this
    }

    /**
     * Включает внутреннее логирование
     */
    open fun enableLogging(config: LoggerConfig?): BaseConfigBuilder? {
        loggerConfig = config
        return this
    }

    /**
     * Устанавливает важность пуш-уведомлений. Принимает константы NotificationManager.
     * По умолчанию IMPORTANCE_DEFAULT
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    open fun setNotificationImportance(importance: Int): BaseConfigBuilder? {
        notificationImportance = importance
        return this
    }

    /**
     * Устанавливает версию апи для запросов. По умолчанию v15
     */
    open fun setApiVersion(apiVersion: ApiVersionEnum) {
        this.apiVersion = apiVersion
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
            allowUntrustedSSLCertificate,
            keepSocketActive,
            apiVersion
        )
    }
}
