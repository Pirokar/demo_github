package im.threads.ui.config

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import im.threads.business.config.BaseConfigBuilder
import im.threads.business.core.UnreadMessagesCountListener
import im.threads.business.logger.LoggerConfig
import im.threads.business.rest.config.RequestConfig
import im.threads.ui.ChatStyle
import im.threads.ui.activities.ChatActivity
import im.threads.ui.core.PendingIntentCreator
import okhttp3.Interceptor

class ConfigBuilder(context: Context) : BaseConfigBuilder(context) {
    private var isAttachmentsEnabled: Boolean? = null
    private var lightTheme: ChatStyle? = null
    private var darkTheme: ChatStyle? = null
    private var isCustomPendingIntentCreatorInstalled = false

    private var pendingIntentCreator: PendingIntentCreator =
        object : PendingIntentCreator {
            override fun create(context: Context, appMarker: String?): PendingIntent? {
                val i = Intent(
                    context,
                    ChatActivity::class.java
                )
                i.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                var flags = PendingIntent.FLAG_CANCEL_CURRENT
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    flags = flags or PendingIntent.FLAG_IMMUTABLE
                }
                return PendingIntent.getActivity(context, 0, i, flags)
            }
        }

    fun pendingIntentCreator(pendingIntentCreator: PendingIntentCreator): ConfigBuilder {
        this.pendingIntentCreator = pendingIntentCreator
        isCustomPendingIntentCreatorInstalled = true
        return this
    }

    override fun keepSocketActive(keepSocketActive: Boolean): ConfigBuilder {
        super.keepSocketActive(keepSocketActive)
        return this
    }

    override fun serverBaseUrl(serverBaseUrl: String?): ConfigBuilder {
        super.serverBaseUrl(serverBaseUrl)
        return this
    }

    override fun datastoreUrl(datastoreUrl: String?): ConfigBuilder {
        super.datastoreUrl(datastoreUrl)
        return this
    }

    override fun threadsGateUrl(threadsGateUrl: String?): ConfigBuilder {
        super.threadsGateUrl(threadsGateUrl)
        return this
    }

    override fun threadsGateProviderUid(threadsGateProviderUid: String?): ConfigBuilder {
        super.threadsGateProviderUid(threadsGateProviderUid)
        return this
    }

    override fun unreadMessagesCountListener(unreadMessagesCountListener: UnreadMessagesCountListener?): ConfigBuilder {
        super.unreadMessagesCountListener(unreadMessagesCountListener)
        return this
    }

    override fun isDebugLoggingEnabled(isDebugLoggingEnabled: Boolean): ConfigBuilder {
        super.isDebugLoggingEnabled(isDebugLoggingEnabled)
        return this
    }

    override fun historyLoadingCount(historyLoadingCount: Int): ConfigBuilder {
        super.historyLoadingCount(historyLoadingCount)
        return this
    }

    override fun surveyCompletionDelay(surveyCompletionDelay: Int): ConfigBuilder {
        super.surveyCompletionDelay(surveyCompletionDelay)
        return this
    }

    override fun requestConfig(requestConfig: RequestConfig): ConfigBuilder {
        super.requestConfig(requestConfig)
        return this
    }

    override fun trustedSSLCertificates(trustedSSLCertificates: List<Int>?): ConfigBuilder {
        super.trustedSSLCertificates(trustedSSLCertificates)
        return this
    }

    override fun networkInterceptor(interceptor: Interceptor?): ConfigBuilder {
        super.networkInterceptor(interceptor)
        return this
    }

    override fun setNewChatCenterApi(): ConfigBuilder {
        super.isNewChatCenterApi = true
        return this
    }

    /**
     * Применяет настройки светлой темы
     * @param lightTheme набор параметров для светлой темы. Передайте null, если хотите отключить светлую тему
     */
    fun applyLightTheme(lightTheme: ChatStyle?): ConfigBuilder {
        this.lightTheme = lightTheme
        return this
    }

    /**
     * Применяет настройки темной темы
     * @param darkTheme набор параметров для темной темы. Передайте null, если хотите отключить темную тему
     */
    fun applyDarkTheme(darkTheme: ChatStyle?): ConfigBuilder {
        this.darkTheme = darkTheme
        return this
    }

    fun showAttachmentsButton(): ConfigBuilder {
        isAttachmentsEnabled = true
        return this
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun setNotificationImportance(importance: Int): ConfigBuilder {
        super.setNotificationImportance(importance)
        return this
    }

    override fun enableLogging(config: LoggerConfig?): ConfigBuilder {
        super.loggerConfig = config
        return this
    }

    override fun build(): Config {
        return Config(
            context,
            serverBaseUrl,
            datastoreUrl,
            threadsGateUrl,
            threadsGateProviderUid,
            isNewChatCenterApi,
            apiVersion,
            loggerConfig,
            pendingIntentCreator,
            unreadMessagesCountListener,
            networkInterceptor,
            lightTheme,
            darkTheme,
            isDebugLoggingEnabled,
            historyLoadingCount,
            surveyCompletionDelay,
            requestConfig,
            trustedSSLCertificates,
            allowUntrustedSSLCertificate,
            keepSocketActive,
            notificationLevel,
            isAttachmentsEnabled
        )
    }

    override fun toString(): String {
        return "Config. " +
            "isAttachmentsEnabled: $isAttachmentsEnabled | " +
            "is custom pending intent creator installed: $isCustomPendingIntentCreatorInstalled | " +
            "serverBaseUrl: $serverBaseUrl | " +
            "datastoreUrl: $datastoreUrl | " +
            "threadsGateUrl: $threadsGateUrl | " +
            "threadsGateProviderUid: $threadsGateProviderUid | " +
            "isNewChatCenterApi: $isNewChatCenterApi\n" +
            "apiVersion: ${apiVersion.name}\n" +
            "$loggerConfig\n" +
            "unreadMessagesCountListener is installed: ${unreadMessagesCountListener != null} | " +
            "networkInterceptor is installed: ${networkInterceptor != null} | " +
            "isDebugLoggingEnabled: $isDebugLoggingEnabled | " +
            "historyLoadingCount: $historyLoadingCount | " +
            "surveyCompletionDelay: $surveyCompletionDelay\n" +
            "$requestConfig\n" +
            "notificationLevel: $notificationLevel | " +
            "isAttachmentsEnabled: $isAttachmentsEnabled | " +
            "trustedSSLCertificates count: ${trustedSSLCertificates.count()}\n" +
            "keepSocketActive: $keepSocketActive\n" +
            "lightTheme: $lightTheme\n" +
            "darkTheme: $darkTheme"
    }
}
