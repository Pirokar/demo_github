package im.threads.business

import androidx.test.core.app.ApplicationProvider
import im.threads.business.config.BaseConfig
import im.threads.business.core.ContextHolder
import im.threads.business.rest.config.RequestConfig
import im.threads.business.serviceLocator.core.startEdnaLocator
import im.threads.business.serviceLocator.coreSLModule
import im.threads.business.transport.threadsGate.ThreadsGateTransport
import im.threads.ui.serviceLocator.uiSLModule
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BaseConfigTest {
    private val ednaMockScheme = "http"
    private val ednaMockHost = "localhost"
    private val ednaMockPort = 8080
    private val ednaMockUrl = "$ednaMockScheme://$ednaMockHost:$ednaMockPort/"
    private val ednaMockThreadsGateUrl = "ws://$ednaMockHost:$ednaMockPort/gate/socket"
    private val ednaMockThreadsGateProviderUid = "TEST_93jLrtnipZsfbTddRfEfbyfEe5LKKhTl"
    private lateinit var config: BaseConfig

    @Before
    fun before() {
        ContextHolder.context = ApplicationProvider.getApplicationContext()
        startEdnaLocator { modules(coreSLModule, uiSLModule) }
        config = getEmptyBaseConfig()
    }

    @Test
    fun whenUpdateTransport_thenTransportUpdated() {
        val testCertificate: Int = im.threads.test.R.raw.edna
        val testThreadsGateUrl = "${ednaMockThreadsGateUrl}test"
        val testThreadsGateProviderUid = "${ednaMockThreadsGateProviderUid}test"

        config.updateTransport(
            testThreadsGateUrl,
            testThreadsGateProviderUid,
            listOf(testCertificate)
        )
        assert(
            config.trustedSSLCertificates?.isNotEmpty() == true &&
                config.trustedSSLCertificates!![0] == testCertificate &&
                (config.transport as ThreadsGateTransport).threadsGateUrl == testThreadsGateUrl &&
                (config.transport as ThreadsGateTransport).threadsGateProviderUid == testThreadsGateProviderUid
        )
    }

    private fun getEmptyBaseConfig() = BaseConfig(
        ApplicationProvider.getApplicationContext(),
        serverBaseUrl = ednaMockUrl,
        datastoreUrl = ednaMockUrl,
        threadsGateUrl = ednaMockThreadsGateUrl,
        threadsGateProviderUid = ednaMockThreadsGateProviderUid,
        isNewChatCenterApi = false,
        loggerConfig = null,
        unreadMessagesCountListener = null,
        networkInterceptor = null,
        isDebugLoggingEnabled = false,
        historyLoadingCount = 20,
        surveyCompletionDelay = 5000,
        requestConfig = RequestConfig(),
        notificationImportance = 0,
        trustedSSLCertificates = null,
        allowUntrustedSSLCertificate = false,
        keepSocketActive = false
    )
}
