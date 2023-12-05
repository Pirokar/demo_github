package im.threads.business.config

import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import im.threads.business.core.UnreadMessagesCountListener
import im.threads.business.logger.LoggerConfig
import im.threads.business.logger.NetworkLoggerInterceptor
import im.threads.business.rest.config.RequestConfig
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BaseConfigBuilderTest : ConfigTestBaseClass() {
    private lateinit var configBuilder: BaseConfigBuilder

    @Before
    override fun before() {
        super.before()
        configBuilder = getEmptyBaseConfigBuilder()
    }

    @Test
    fun whenSetServerBaseUrl_thenItExistsInObject() {
        configBuilder.serverBaseUrl(ednaMockUrl)
        assert(configBuilder.serverBaseUrl == ednaMockUrl)
    }

    @Test
    fun whenSetServerBaseUrlNoSlash_thenItExistsInObjectWithSlash() {
        configBuilder.serverBaseUrl("$ednaMockScheme://$ednaMockHost:$ednaMockPort")
        assert(configBuilder.serverBaseUrl == ednaMockUrl)
    }

    @Test
    fun whenSetDatastoreUrl_thenItExistsInObject() {
        configBuilder.datastoreUrl(ednaMockUrl)
        assert(configBuilder.datastoreUrl == ednaMockUrl)
    }

    @Test
    fun whenSetDatastoreUrlNoSlash_thenItExistsInObjectWithSlash() {
        configBuilder.datastoreUrl("$ednaMockScheme://$ednaMockHost:$ednaMockPort")
        assert(configBuilder.datastoreUrl == ednaMockUrl)
    }

    @Test
    fun whenKeepSocketActiveTrue_thenItTrueInObject() {
        configBuilder.keepSocketActive(true)
        assert(configBuilder.keepSocketActive)
    }

    @Test
    fun whenSetThreadsGateUrl_thenItExistsInObject() {
        configBuilder.threadsGateUrl(ednaMockThreadsGateUrl)
        assert(configBuilder.threadsGateUrl == ednaMockThreadsGateUrl)
    }

    @Test
    fun whenSetThreadsGateUrlWithSlash_thenItExistsInObject() {
        configBuilder.threadsGateUrl("$ednaMockThreadsGateUrl/")
        assert(configBuilder.threadsGateUrl == ednaMockThreadsGateUrl)
    }

    @Test
    fun whenSetThreadsGateProviderUid_thenItExistsInObject() {
        configBuilder.threadsGateProviderUid(ednaMockThreadsGateProviderUid)
        assert(configBuilder.threadsGateProviderUid == ednaMockThreadsGateProviderUid)
    }

    @Test
    fun whenSetUnreadMessagesCountListener_thenItExistsInObject() {
        val listener = object : UnreadMessagesCountListener {
            override fun onUnreadMessagesCountChanged(count: Int) {}
        }
        configBuilder.unreadMessagesCountListener(listener)
        assert(configBuilder.unreadMessagesCountListener == listener)
    }

    @Test
    fun whenSetIsDebugLoggingEnabledTrue_thenItTrueInObject() {
        configBuilder.isDebugLoggingEnabled(true)
        assert(configBuilder.isDebugLoggingEnabled)
    }

    @Test
    fun whenSetHistoryLoadingCount_thenItExistsInObject() {
        val count = 30
        configBuilder.historyLoadingCount(count)
        assert(configBuilder.historyLoadingCount == count)
    }

    @Test
    fun whenSetSurveyCompletionDelay_thenItExistsInObject() {
        val delay = 30
        configBuilder.surveyCompletionDelay(delay)
        assert(configBuilder.surveyCompletionDelay == delay)
    }

    @Test
    fun whenSetRequestConfig_thenItExistsInObject() {
        val requestConfig = RequestConfig().apply {
            socketClientSettings.resendIntervalMillis = 2000
        }
        configBuilder.requestConfig(requestConfig)
        assert(configBuilder.requestConfig == requestConfig)
    }

    @Test
    fun whenSetTrustedSSLCertificates_thenItExistsInObject() {
        val testCertificate: Int = im.threads.test.R.raw.edna
        configBuilder.trustedSSLCertificates(listOf(testCertificate))
        assert(configBuilder.trustedSSLCertificates.isNotEmpty() && configBuilder.trustedSSLCertificates[0] == testCertificate)
    }

    @Test
    fun whenSetAllowUntrustedSSLCertificatesTrue_thenItTrueInObject() {
        configBuilder.allowUntrustedSSLCertificates(true)
        assert(configBuilder.allowUntrustedSSLCertificate)
    }

    @Test
    fun whenSetNetworkInterceptor_thenItExistsInObject() {
        val interceptor = NetworkLoggerInterceptor()
        configBuilder.networkInterceptor(interceptor)
        assert(configBuilder.networkInterceptor == interceptor)
    }

    @Test
    fun whenSetNewChatCenterApiTrue_thenItTrueInObject() {
        configBuilder.setNewChatCenterApi()
        assert(configBuilder.isNewChatCenterApi)
    }

    @Test
    fun whenEnableLogging_thenItEnabledInObject() {
        val loggerConfig = LoggerConfig.Builder(ApplicationProvider.getApplicationContext())
            .build()
        configBuilder.enableLogging(loggerConfig)
        assert(configBuilder.loggerConfig == loggerConfig)
    }

    @Test
    fun whenSetNotificationImportance_thenItExistsInObject() {
        val importance = NotificationManager.IMPORTANCE_MAX
        configBuilder.setNotificationImportance(importance)
        assert(configBuilder.notificationImportance == importance)
    }

    @Test
    fun whenBuildConfig_thenBaseConfigCorrect() {
        val listener = object : UnreadMessagesCountListener {
            override fun onUnreadMessagesCountChanged(count: Int) {}
        }
        val requestConfig = RequestConfig().apply {
            socketClientSettings.resendIntervalMillis = 2000
        }
        val historyLoadingCount = 30
        val surveyCompletionDelay = 50
        val testCertificate: Int = im.threads.test.R.raw.edna
        val interceptor = NetworkLoggerInterceptor()
        val loggerConfig = LoggerConfig.Builder(ApplicationProvider.getApplicationContext())
            .build()
        val importance = NotificationManager.IMPORTANCE_MAX

        configBuilder.apply {
            serverBaseUrl(ednaMockUrl)
            datastoreUrl(ednaMockUrl)
            keepSocketActive(true)
            threadsGateUrl(ednaMockThreadsGateUrl)
            threadsGateProviderUid(ednaMockThreadsGateProviderUid)
            unreadMessagesCountListener(listener)
            isDebugLoggingEnabled(true)
            historyLoadingCount(historyLoadingCount)
            surveyCompletionDelay(surveyCompletionDelay)
            requestConfig(requestConfig)
            trustedSSLCertificates(listOf(testCertificate))
            allowUntrustedSSLCertificates(true)
            networkInterceptor(interceptor)
            setNewChatCenterApi()
            enableLogging(loggerConfig)
            setNotificationImportance(importance)
        }
        val baseConfig = configBuilder.build()
        assert(
            baseConfig.serverBaseUrl == ednaMockUrl &&
                baseConfig.datastoreUrl == ednaMockUrl &&
                baseConfig.keepSocketActive &&
                baseConfig.threadsGateUrl == ednaMockThreadsGateUrl &&
                baseConfig.threadsGateProviderUid == ednaMockThreadsGateProviderUid &&
                baseConfig.unreadMessagesCountListener == listener &&
                baseConfig.isDebugLoggingEnabled &&
                baseConfig.historyLoadingCount == historyLoadingCount &&
                baseConfig.surveyCompletionDelay == surveyCompletionDelay &&
                baseConfig.requestConfig == requestConfig &&
                baseConfig.trustedSSLCertificates!![0] == testCertificate &&
                baseConfig.allowUntrustedSSLCertificate &&
                baseConfig.networkInterceptor == interceptor &&
                baseConfig.isNewChatCenterApi &&
                baseConfig.loggerConfig == loggerConfig &&
                baseConfig.notificationImportance == importance
        )
    }

    private fun getEmptyBaseConfigBuilder() = BaseConfigBuilder(ApplicationProvider.getApplicationContext())
}
