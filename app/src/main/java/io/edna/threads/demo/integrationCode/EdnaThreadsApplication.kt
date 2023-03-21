package io.edna.threads.demo.integrationCode

import android.app.Application
import android.content.Context
import im.threads.business.logger.LoggerConfig
import im.threads.business.logger.LoggerRetentionPolicy
import im.threads.ui.config.ConfigBuilder
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.appCode.business.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.io.File

class EdnaThreadsApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@EdnaThreadsApplication)
            modules(appModule)
        }

        val loggerConfig = LoggerConfig.Builder(this)
            .logToFile()
            .dir(File(this.filesDir, "logs"))
            .retentionPolicy(LoggerRetentionPolicy.TOTAL_SIZE)
            .maxTotalSize(5242880)
            .build()

        val configBuilder = ConfigBuilder(this)
            .surveyCompletionDelay(2000)
            .historyLoadingCount(50)
            .isDebugLoggingEnabled(true)
            .showAttachmentsButton()
            .enableLogging(loggerConfig)
            .disableSSLPinning() as ConfigBuilder

        getTransportConfig(this).apply {
            configBuilder.serverBaseUrl(baseUrl)
                .datastoreUrl(datastoreUrl)
                .threadsGateUrl(threadsGateUrl)
                .threadsGateProviderUid(threadsGateProviderUid)

            if (isNewChatCenterApi) {
                configBuilder.setNewChatCenterApi()
            }
        }

        ThreadsLib.init(configBuilder)
    }

    private fun getTransportConfig(ctx: Context?): TransportConfig {
        val baseUrl = "https://mobile4.dev.flex.mfms.ru"
        val datastoreUrl = "https://mobile4.dev.flex.mfms.ru"
        val threadsGateUrl = "ws://mobile4.dev.flex.mfms.ru/gate/socket"
        val threadsGateProviderUid = "MOBILE4_HwZ9QhTihb2d8U3I17dBHy1NB9vA9XVkMz65"
        val isNewChatCenterApi = true
        return TransportConfig(
            baseUrl = baseUrl,
            datastoreUrl = datastoreUrl,
            threadsGateUrl = threadsGateUrl,
            threadsGateProviderUid = threadsGateProviderUid,
            isNewChatCenterApi
        )
    }
}
