package edna.chatcenter.demo

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import edna.chatcenter.demo.appCode.models.ServerConfig
import edna.chatcenter.demo.appCode.models.TestData
import edna.chatcenter.demo.appCode.models.UserInfo
import edna.chatcenter.demo.integrationCode.ednaMockThreadsGateProviderUid
import edna.chatcenter.demo.integrationCode.ednaMockThreadsGateUrl
import edna.chatcenter.demo.integrationCode.ednaMockUrl
import edna.chatcenter.demo.kaspressoSreens.ChatMainScreen
import edna.chatcenter.demo.kaspressoSreens.DemoLoginScreen
import edna.chatcenter.ui.core.ChatAuthType
import edna.chatcenter.ui.core.config.ChatAuth
import edna.chatcenter.ui.core.config.ChatConfigCore
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
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
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
import java.util.concurrent.TimeUnit

abstract class BaseTestCase(
    private val isUserInputEnabled: Boolean = true
) : TestCase() {
    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    private val userId = (10000..99999).random().toString()
    protected val transportConfig = ChatTransportConfig(
        ednaMockUrl,
        ednaMockThreadsGateUrl,
        ednaMockUrl,
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

    private val apiTestServer = MockWebServer()
    private val datastoreTestServer = MockWebServer()

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
    }

    @Before
    open fun before() {
        startTestServer()
    }

    @After
    open fun after() {
        stopTestServer()
    }

    protected fun applyDefaultUserToDemoApp(noUserId: Boolean = false) {
        val testData = getExistedTestData().copy(
            userInfo = UserInfo(userId = if (noUserId) null else userId)
        )
        BuildConfig.TEST_DATA.set(testData.toJson())
    }

    protected fun startTestServer() {
        apiTestServer.start(8080)
        datastoreTestServer.start(8090)

        val baseUrl: HttpUrl = apiTestServer.url("/")
        val datastoreUrl: HttpUrl = datastoreTestServer.url("/")

        edna.chatcenter.ui.BuildConfig.TEST_BASE_URL.set(baseUrl)
        edna.chatcenter.ui.BuildConfig.TEST_DATASTORE_URL.set(datastoreUrl)
    }

    protected fun stopTestServer() {
        try {
            apiTestServer.shutdown()
            datastoreTestServer.shutdown()
        } catch (exc: Exception) {
            exc.printStackTrace()
        }
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
                    Log.i("WS_MOCK_APPLIED", wsAnswer)
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
        withAnswerDelayInMs: Long = 0,
        historyAnswer: String? = null,
        configAnswer: String? = null
    ) {
        BuildConfig.IS_MOCK_WEB_SERVER.set(true)

        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path ?: ""

                return if (path.contains("search")) {
                    MockResponse()
                        .setResponseCode(200)
                        .setBody(TestMessages.searchEdnHttpMock)
                        .addHeader("Content-Type", "application/json")
                } else if (path.contains("history")) {
                    MockResponse()
                        .setResponseCode(200)
                        .setBody(historyAnswer ?: TestMessages.emptyHistoryMessage)
                        .setBodyDelay(withAnswerDelayInMs, TimeUnit.MILLISECONDS)
                        .addHeader("Content-Type", "application/json")
                } else if (path.contains("config")) {
                    MockResponse()
                        .setResponseCode(200)
                        .setBody(configAnswer ?: TestMessages.defaultConfigMock)
                        .addHeader("Content-Type", "application/json")
                } else {
                    MockResponse().setResponseCode(404)
                }
            }
        }

        val datastoreDispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path ?: ""

                return if (path.contains("files")) {
                    MockResponse()
                        .setResponseCode(200)
                        .setBody("{\"result\":\"20231013-8054e1c4-ccd4-44da-ac96-d5dc2c4c1601.jpg\"}")
                        .addHeader("Content-Type", "application/json")
                        .setBodyDelay(withAnswerDelayInMs, TimeUnit.MILLISECONDS)
                } else {
                    MockResponse().setResponseCode(404)
                }
            }
        }

        apiTestServer.dispatcher = dispatcher
        datastoreTestServer.dispatcher = datastoreDispatcher
    }

    protected fun prepareHttpErrorMocks(withAnswerDelayInMs: Long = 0) {
        BuildConfig.IS_MOCK_WEB_SERVER.set(true)

        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path ?: ""

                return if (path.contains("history")) {
                    MockResponse()
                        .setResponseCode(404)
                        .setBody(TestMessages.emptyHistoryMessage)
                        .setBodyDelay(withAnswerDelayInMs, TimeUnit.MILLISECONDS)
                        .addHeader("Content-Type", "application/json")
                } else if (path.contains("config")) {
                    MockResponse()
                        .setResponseCode(404)
                        .setBody(TestMessages.defaultConfigMock)
                        .addHeader("Content-Type", "application/json")
                } else {
                    MockResponse().setResponseCode(404)
                }
            }
        }

        apiTestServer.dispatcher = dispatcher
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

    protected fun getSocketTimeout(): Long {
        return try {
            val config: ChatConfigCore by inject()
            config.networkConfig.wsConfig.connectionTimeout * 1000L
        } catch (exc: Exception) {
            30000L
        }
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
        threadsGateProviderUid = ednaMockThreadsGateProviderUid,
        datastoreUrl = ednaMockUrl,
        serverBaseUrl = ednaMockUrl,
        threadsGateUrl = ednaMockThreadsGateUrl,
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
