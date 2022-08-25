package im.threads.ui.config

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import im.threads.ThreadsLib.PendingIntentCreator
import im.threads.business.config.BaseConfigBuilder
import im.threads.view.ChatActivity

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

    fun pendingIntentCreator(pendingIntentCreator: PendingIntentCreator): BaseConfigBuilder {
        this.pendingIntentCreator = pendingIntentCreator
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
