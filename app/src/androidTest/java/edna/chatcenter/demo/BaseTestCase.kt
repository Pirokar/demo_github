package edna.chatcenter.demo

import android.content.Context
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import edna.chatcenter.demo.appCode.models.ServerConfig
import edna.chatcenter.demo.appCode.models.TestData
import edna.chatcenter.demo.appCode.models.UserInfo
import edna.chatcenter.demo.kaspressoSreens.ChatMainScreen
import edna.chatcenter.demo.kaspressoSreens.DemoLoginScreen
import edna.chatcenter.ui.core.ChatAuthType
import edna.chatcenter.ui.core.config.ChatAuth
import edna.chatcenter.ui.core.config.ChatUser
import edna.chatcenter.ui.core.config.transport.ChatNetworkConfig
import edna.chatcenter.ui.core.config.transport.ChatTransportConfig
import edna.chatcenter.ui.core.config.transport.HTTPConfig
import edna.chatcenter.ui.core.config.transport.SSLPinningConfig
import edna.chatcenter.ui.core.config.transport.WSConfig
import edna.chatcenter.ui.core.serviceLocator.core.inject
import edna.chatcenter.ui.core.transport.websocketGate.WebsocketTransport
import edna.chatcenter.ui.visual.ChatConfig
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
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

abstract class BaseTestCase(private val isUserInputEnabled: Boolean = true) : TestCase() {
    private val userId = (10000..99999).random().toString()
    protected val transportConfig = ChatTransportConfig(
        edna.chatcenter.demo.appCode.ednaMockUrl,
        edna.chatcenter.demo.appCode.ednaMockThreadsGateUrl,
        edna.chatcenter.demo.appCode.ednaMockUrl,
        dataStoreHTTPHeaders = mapOf()
    )
    protected val networkConfig = ChatNetworkConfig(
        HTTPConfig(
            10,
            10,
            10
        ),
        WSConfig(
            20,
            20,
            15
        ),
        SSLPinningConfig(
            allowUntrustedCertificates = true
        )
    )
    protected val config = ChatConfig(
        transportConfig,
        networkConfig,
        true,
        searchEnabled = true,
        linkPreviewEnabled = true,
        voiceRecordingEnabled = true,
        autoScrollToLatest = true,
        historyLoadingCount = 50
    )
    protected val providerUid = "testProviderUid"
    protected val appMarker = "testAppMarker"
    protected val user = ChatUser("userTest:$userId", "Vladimir", mapOf(Pair("specialKey", "w33")))
    protected val auth = ChatAuth("sdfw34r43", "retail", ChatAuthType.COOKIES, "23t5ef", true)

    private val coroutineScope = CoroutineScope(Dispatchers.Unconfined)

    protected val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    protected var wsMocksMap = getDefaultWsMocksMap()

    protected val helloTextToSend = "Hello, Edna! This is a test message"

    @get:Rule
    val wireMockRule = WireMockRule(
        WireMockConfiguration.wireMockConfig().apply {
            port(edna.chatcenter.demo.appCode.ednaMockPort)
        },
        false
    )

    @Mock
    protected lateinit var okHttpClient: OkHttpClient

    @Mock
    protected lateinit var socket: WebSocket

    private val socketListener: WebsocketTransport.WebSocketListener by lazy {
        val transport: WebsocketTransport by inject()
        transport.listener
    }

    init {
        val testData = getExistedTestData().copy(
            serverConfig = getDefaultServerConfig()
        )
        BuildConfig.TEST_DATA.set(testData.toJson())
        edna.chatcenter.ui.BuildConfig.IS_ANIMATIONS_DISABLED.set(true)
    }

    protected fun applyDefaultUserToDemoApp(noUserId: Boolean = false) {
        val testData = getExistedTestData().copy(
            userInfo = UserInfo(userId = if (noUserId) null else userId)
        )
        BuildConfig.TEST_DATA.set(testData.toJson())
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

    protected fun prepareWsMocks(t: Throwable? = null) {
        BuildConfig.IS_MOCK_WEB_SERVER.set(true)
        MockitoAnnotations.openMocks(this)
        Mockito.`when`(okHttpClient.newWebSocket(anyOrNull(), anyOrNull())).thenReturn(socket)
        Mockito.doAnswer { mock: InvocationOnMock ->
            if (t != null) {
                sendErrorMessageToSocket(t)
            } else {
                val stringArg = mock.arguments[0] as String
                val answer = getAnswersForWebSocket(stringArg)
                answer?.let { wsAnswer ->
                    sendMessageToSocket(wsAnswer)
                }
            }
            null
        }.`when`(socket).send(Mockito.anyString())

        coroutineScope.launch {
            WebsocketTransport.transportUpdatedChannel.collect {
                it.client = okHttpClient
                it.webSocket = null
            }
        }
    }

    protected fun prepareHttpMocks(
        withAnswerDelayInMs: Int = 0,
        historyAnswer: String? = null,
        configAnswer: String? = null
    ) {
        BuildConfig.IS_MOCK_WEB_SERVER.set(true)
        wireMockRule.stubFor(
            WireMock.get(WireMock.urlPathMatching(".*/search.*"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody(TestMessages.searchEdnHttpMock)
                        .withHeader("Content-Type", "application/json")
                )
        )
        wireMockRule.stubFor(
            WireMock.get(WireMock.urlPathMatching(".*/history.*"))
                .willReturn(
                    WireMock.aResponse()
                        .withBody(historyAnswer ?: TestMessages.emptyHistoryMessage)
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(withAnswerDelayInMs)
                )
        )
        wireMockRule.stubFor(
            WireMock.put(WireMock.urlEqualTo("/files"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody(
                            "{\"result\":\"20231013-8054e1c4-ccd4-44da-ac96-d5dc2c4c1601.jpg\"," +
                                "\"optional\":{\"name\":\"test_image2.jpg\",\"type\":\"image/jpeg\",\"size\":617636},\"state\":\"READY\"}"
                        )
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(withAnswerDelayInMs)
                )
        )
        wireMockRule.stubFor(
            WireMock.get(WireMock.urlPathMatching(".*/config.*"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody(configAnswer ?: TestMessages.defaultConfigMock)
                        .withHeader("Content-Type", "application/json")
                )
        )
    }

    protected fun prepareHttpErrorMocks(withAnswerDelayInMs: Int = 0) {
        BuildConfig.IS_MOCK_WEB_SERVER.set(true)
        wireMockRule.stubFor(
            WireMock.get(WireMock.urlPathMatching(".*/history.*"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(404)
                        .withBody(TestMessages.emptyHistoryMessage)
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(withAnswerDelayInMs)
                )
        )
        wireMockRule.stubFor(
            WireMock.get(WireMock.urlPathMatching(".*/config.*"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(404)
                        .withBody(TestMessages.defaultConfigMock)
                        .withHeader("Content-Type", "application/json")
                )
        )
    }

    protected fun getDefaultWsMocksMap() = HashMap<String, String>().apply {
        put("registerDevice", TestMessages.registerDeviceWsAnswer)
        put("INIT_CHAT", TestMessages.initChatWsAnswer)
        put("CLIENT_INFO", TestMessages.clientInfoWsAnswer)
    }

    protected fun readTextFileFromRawResourceId(resourceId: Int): String {
        var string: String? = ""
        val stringBuilder = StringBuilder()
        val inputStream: InputStream = context.resources.openRawResource(resourceId)
        val reader = BufferedReader(InputStreamReader(inputStream))
        while (true) {
            try {
                if (reader.readLine().also { string = it } == null) break
            } catch (e: IOException) {
                e.printStackTrace()
            }
            stringBuilder.append(string).append("\n")
        }
        inputStream.close()
        return stringBuilder.toString()
    }

    protected fun sendHelloMessageFromUser() {
        ChatMainScreen {
            inputEditView {
                assert("Поле ввода должно быть видимым") { isVisible() }
            }
            welcomeScreen {
                assert("Экран приветствия должен быть видим") { isVisible() }
            }

            inputEditView.typeText(helloTextToSend)
            sendMessageBtn {
                assert("Кнопка отправки сообщений должна быть видимой") { isVisible() }
                click()
            }
        }
    }

    protected fun sendCustomMessageFromUser(message: String) {
        ChatMainScreen {
            inputEditView {
                assert("Поле ввода должно быть видимым") { isVisible() }
            }

            inputEditView.typeText(message)
            sendMessageBtn {
                assert("Кнопка отправки сообщений должна быть видимой") { isVisible() }
                click()
            }
        }
    }

    private fun getAnswersForWebSocket(
        websocketMessage: String
    ): String? {
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
                        answer
                    } else {
                        wsMocksMap[key]
                    }
                } else {
                    wsMocksMap[key]
                }
            }
        }
        return null
    }

    private fun getDefaultServerConfig() = ServerConfig(
        name = "TestServer",
        threadsGateProviderUid = edna.chatcenter.demo.appCode.ednaMockThreadsGateProviderUid,
        datastoreUrl = edna.chatcenter.demo.appCode.ednaMockUrl,
        serverBaseUrl = edna.chatcenter.demo.appCode.ednaMockUrl,
        threadsGateUrl = edna.chatcenter.demo.appCode.ednaMockThreadsGateUrl,
        isShowMenu = true,
        isInputEnabled = isUserInputEnabled
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

fun InputStream.toFile(to: File) {
    this.use { input ->
        to.outputStream().use { out ->
            input.copyTo(out)
        }
    }
}

fun InputStream.copyToUri(to: Uri, context: Context) {
    this.use { input ->
        context.contentResolver.openOutputStream(to).use { out ->
            input.copyTo(out!!)
        }
    }
}
