package im.threads.ui.config

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import im.threads.business.config.BaseConfigBuilder
import im.threads.business.core.UnreadMessagesCountListener
import im.threads.business.logger.LoggerConfig
import im.threads.business.rest.config.RequestConfig
import im.threads.ui.core.PendingIntentCreator
import im.threads.view.ChatActivity
import okhttp3.Interceptor

class ConfigBuilder(context: Context) : BaseConfigBuilder(context) {
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

    override fun threadsGateHCMProviderUid(threadsGateHCMProviderUid: String?): ConfigBuilder {
        super.threadsGateHCMProviderUid(threadsGateHCMProviderUid)
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

    override fun requestConfig(requestConfig: RequestConfig?): ConfigBuilder {
        super.requestConfig(requestConfig)
        return this
    }

    override fun certificateRawResIds(certificateRawResIds: List<Int?>?): ConfigBuilder {
        super.certificateRawResIds(certificateRawResIds)
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
            threadsGateHCMProviderUid,
            isNewChatCenterApi,
            loggerConfig,
            pendingIntentCreator,
            unreadMessagesCountListener,
            networkInterceptor,
            isDebugLoggingEnabled,
            historyLoadingCount,
            surveyCompletionDelay,
            requestConfig,
            certificateRawResIds
        )
    }
}
