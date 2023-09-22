package io.edna.threads.demo

import androidx.test.platform.app.InstrumentationRegistry
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import im.threads.business.UserInfoBuilder
import im.threads.business.config.BaseConfig
import im.threads.business.rest.queries.ednaMockPort
import im.threads.business.rest.queries.ednaMockUrl
import im.threads.business.transport.threadsGate.ThreadsGateTransport
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.appCode.models.ServerConfig
import io.edna.threads.demo.appCode.models.TestData
import io.edna.threads.demo.appCode.models.UserInfo
import io.edna.threads.demo.integrationCode.mainActivity.ednaMockThreadsGateProviderUid
import io.edna.threads.demo.integrationCode.mainActivity.ednaMockThreadsGateUrl
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
    private val userId = (10000..99999).random().toString()

    private val coroutineScope = CoroutineScope(Dispatchers.Unconfined)

    protected val context = InstrumentationRegistry.getInstrumentation().targetContext
    protected var wsMocksMap = getDefaultWsMocksMap()
    protected var clientInfoWsMessages = getDefaultClientInfoWsMessages()

    @get:Rule
    val wireMockRule = WireMockRule(
        WireMockConfiguration.wireMockConfig().apply {
            port(ednaMockPort)
        },
        false
    )

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
    }

    protected fun applyDefaultUserToDemoApp(noUserId: Boolean = false) {
        val testData = getExistedTestData().copy(
            userInfo = UserInfo(userId = if (noUserId) null else userId)
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

    protected fun prepareWsMocks() {
        BuildConfig.IS_MOCK_WEB_SERVER.set(true)
        MockitoAnnotations.openMocks(this)
        Mockito.`when`(okHttpClient.newWebSocket(anyOrNull(), anyOrNull())).thenReturn(socket)
        Mockito.doAnswer { mock: InvocationOnMock ->
            val stringArg = mock.arguments[0] as String
            val (answer, isClientInfo) = getAnswersForWebSocket(stringArg)
            answer?.let { wsAnswer ->
                sendMessageToSocket(wsAnswer)
                if (isClientInfo) {
                    clientInfoWsMessages.forEach { sendMessageToSocket(it) }
                }
            }
            null
        }.`when`(socket).send(Mockito.anyString())

        coroutineScope.launch {
            ThreadsGateTransport.transportUpdatedChannel.collect {
                it.client = okHttpClient
                it.webSocket = null
            }
        }
    }

    protected fun prepareHttpMocks(withAnswerDelayInMs: Int = 0) {
        wireMockRule.stubFor(
            WireMock.get(WireMock.urlPathMatching(".*/history.*"))
                .willReturn(
                    WireMock.aResponse()
                        .withBody(TestMessages.emptyHistoryMessage)
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(withAnswerDelayInMs)
                )
        )
    }

    protected fun getDefaultWsMocksMap() = HashMap<String, String>().apply {
        put("registerDevice", TestMessages.registerDeviceWsAnswer)
        put("INIT_CHAT", TestMessages.initChatWsAnswer)
        put("CLIENT_INFO", TestMessages.clientInfoWsAnswer)
    }

    protected fun getDefaultClientInfoWsMessages() = listOf(
        TestMessages.scheduleWsMessage,
        TestMessages.attachmentSettingsWsMessage
    )

    private fun getAnswersForWebSocket(
        websocketMessage: String
    ): Pair<String?, Boolean> {
        wsMocksMap.keys.forEach { key ->
            if (websocketMessage.contains(key)) {
                val draftAnswer = wsMocksMap[key]
                val correlationIdKey = "correlationId\":\""
                return if (websocketMessage.contains(correlationIdKey) && draftAnswer?.contains(correlationIdKey) == true) {
                    val split = websocketMessage.split(correlationIdKey)
                    if (split.size > 1 && split[1].length > 1) {
                        val endOfCorrelationValueIndex = split[1].indexOf("\"")
                        val correlationId = split[1].subSequence(0, endOfCorrelationValueIndex).toString()
                        val answer = draftAnswer.replace(TestMessages.correlationId, correlationId)
                        Pair(answer, key == "CLIENT_INFO")
                    } else {
                        Pair(wsMocksMap[key], key == "CLIENT_INFO")
                    }
                } else {
                    Pair(wsMocksMap[key], key == "CLIENT_INFO")
                }
            }
        }
        return Pair(null, false)
    }

    private fun getDefaultServerConfig() = ServerConfig(
        name = "TestServer",
        threadsGateProviderUid = ednaMockThreadsGateProviderUid,
        datastoreUrl = ednaMockUrl,
        serverBaseUrl = ednaMockUrl,
        threadsGateUrl = ednaMockThreadsGateUrl,
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
