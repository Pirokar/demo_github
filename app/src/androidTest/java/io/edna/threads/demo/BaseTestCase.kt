package io.edna.threads.demo

import androidx.test.platform.app.InstrumentationRegistry
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import im.threads.business.UserInfoBuilder
import im.threads.business.config.BaseConfig
import im.threads.business.transport.threadsGate.ThreadsGateTransport
import im.threads.ui.controllers.ChatController
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.appCode.models.ServerConfig
import io.edna.threads.demo.appCode.models.TestData
import io.edna.threads.demo.appCode.models.UserInfo
import io.edna.threads.demo.kaspressoSreens.DemoLoginScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import org.junit.Rule
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.anyOrNull

abstract class BaseTestCase : TestCase() {
    private val port = 8000
    protected val localhostUrl = "10.0.2.2"
    protected val testServerBaseUrl = "http://$localhostUrl:$port/"
    protected val testDatastoreUrl = "http://$localhostUrl:$port/"
    protected val testThreadsGateUrl = "ws://$localhostUrl:$port/gate/socket"
    protected val testThreadsGateProviderUid = "TEST_93jLrtnipZsfbTddRfEfbyfEe5LKKhTl"
    protected val testTrustedSSLCertificates: ArrayList<Int>? = null
    protected val testAllowUntrustedSSLCertificate = true
    protected val userId = (10000..99999).random().toString()

    private val coroutineScope = CoroutineScope(Dispatchers.Unconfined)

    protected val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val tgMocksMap = HashMap<String, String>().apply {
        put("registerDevice", TestMessages.registerDeviceWsAnswer)
        put("INIT_CHAT", TestMessages.initChatWsAnswer)
        put("CLIENT_INFO", TestMessages.clientInfoWsAnswer)
    }

    @get:Rule
    val wireMockRule = WireMockRule(port)

    @Mock
    protected lateinit var okHttpClient: OkHttpClient

    @Mock
    protected lateinit var socket: WebSocket

    private val socketListener: ThreadsGateTransport.WebSocketListener by lazy {
        (BaseConfig.getInstance().transport as ThreadsGateTransport).listener
    }

    init {
        val testData = getExistedTestData().copy(
            serverConfig = getDefaultServerConfig()
        )
        BuildConfig.TEST_DATA.set(testData.toJson())
        im.threads.BuildConfig.IS_ANIMATIONS_DISABLED.set(true)
        ChatController.getInstance().cleanAll()
    }

    protected fun applyServerSettings(
        serverBaseUrl: String? = null,
        datastoreUrl: String? = null,
        threadsGateUrl: String? = null
    ) {
        ThreadsLib.changeServerSettings(
            serverBaseUrl ?: testServerBaseUrl,
            datastoreUrl ?: testDatastoreUrl,
            threadsGateUrl ?: testThreadsGateUrl,
            testThreadsGateProviderUid,
            testTrustedSSLCertificates,
            testAllowUntrustedSSLCertificate
        )
    }

    protected fun applyDefaultUserToDemoApp() {
        val testData = getExistedTestData().copy(
            userInfo = UserInfo(userId = userId)
        )
        BuildConfig.TEST_DATA.set(testData.toJson())
    }

    protected fun initUserDirectly() {
        ThreadsLib.getInstance().initUser(UserInfoBuilder(userId), true)
    }

    protected fun sendMessageToSocket(message: String) {
        socketListener.onMessage(socket, message)
    }

    protected fun sendErrorMessageToSocket(throwable: Throwable) {
        socketListener.onFailure(socket, throwable, null)
    }

    protected fun openChatFromDemoLoginPage() {
        DemoLoginScreen {
            loginButton {
                click()
            }
        }
    }

    protected fun prepareMocks(mocksMap: HashMap<String, String>? = null) {
        im.threads.BuildConfig.IS_MOCK_WEB_SERVER.set(true)
        MockitoAnnotations.openMocks(this)
        Mockito.`when`(okHttpClient.newWebSocket(anyOrNull(), anyOrNull())).thenReturn(socket)
        Mockito.doAnswer { mock: InvocationOnMock ->
            val stringArg = mock.arguments[0] as String
            val (answer, isClientInfo) = getAnswersForWebSocket(stringArg, mocksMap)
            answer?.let {
                sendMessageToSocket(it)
                if (isClientInfo) {
                    sendMessageToSocket(TestMessages.scheduleWsMessage)
                    sendMessageToSocket(TestMessages.attachmentSettingsWsMessage)
                }
            }
            null
        }.`when`(socket).send(Mockito.anyString())

        wireMockRule.stubFor(
            WireMock.get(WireMock.urlEqualTo("/history"))
                .willReturn(
                    WireMock.aResponse()
                        .withBody(TestMessages.emptyHistoryMessage)
                        .withHeader("Content-Type", "application/json")
                )
        )

        coroutineScope.launch {
            ThreadsGateTransport.transportUpdatedChannel.collect {
                it.client = okHttpClient
                it.webSocket = null
            }
        }
    }

    private fun getAnswersForWebSocket(
        websocketMessage: String,
        mocksMap: HashMap<String, String>? = null
    ): Pair<String?, Boolean> {
        val socketMocksMap = mocksMap ?: tgMocksMap
        socketMocksMap.keys.forEach { key ->
            if (websocketMessage.contains(key)) {
                return Pair(socketMocksMap[key], key == "CLIENT_INFO")
            }
        }
        return Pair(null, false)
    }

    private fun getDefaultServerConfig() = ServerConfig(
        name = "TestServer",
        threadsGateProviderUid = testThreadsGateProviderUid,
        datastoreUrl = testDatastoreUrl,
        serverBaseUrl = testServerBaseUrl,
        threadsGateUrl = testThreadsGateUrl,
        isShowMenu = true,
        allowUntrustedSSLCertificate = true
    )

    private fun getExistedTestData(): TestData {
        val existedTestData = BuildConfig.TEST_DATA.get() as? String ?: ""
        return if (existedTestData.isNotEmpty()) {
            TestData.fromJson(existedTestData)
        } else {
            TestData()
        }
    }
}
