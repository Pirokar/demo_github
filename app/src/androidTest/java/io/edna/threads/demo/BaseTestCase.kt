package io.edna.threads.demo

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.test.platform.app.InstrumentationRegistry
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.kaspersky.kaspresso.internal.extensions.other.createFileIfNeeded
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import im.threads.R
import im.threads.business.UserInfoBuilder
import im.threads.business.config.BaseConfig
import im.threads.business.rest.queries.ednaMockPort
import im.threads.business.rest.queries.ednaMockUrl
import im.threads.business.transport.threadsGate.ThreadsGateTransport
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.appCode.ednaMockThreadsGateProviderUid
import io.edna.threads.demo.appCode.ednaMockThreadsGateUrl
import io.edna.threads.demo.appCode.models.ServerConfig
import io.edna.threads.demo.appCode.models.TestData
import io.edna.threads.demo.appCode.models.UserInfo
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
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
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

abstract class BaseTestCase : TestCase() {
    private val userId = (10000..99999).random().toString()

    private val coroutineScope = CoroutineScope(Dispatchers.Unconfined)

    protected val context = InstrumentationRegistry.getInstrumentation().targetContext
    protected var wsMocksMap = getDefaultWsMocksMap()
    protected var clientInfoWsMessages = getDefaultClientInfoWsMessages()

    protected val helloTextToSend = "Hello, Edna! This is a test message"

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

    protected fun prepareHttpMocks(
        withAnswerDelayInMs: Int = 0,
        historyAnswer: String? = null
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

    protected fun copyFileToDownloads(assetsPath: String): String? {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            copyToDownloadsApiBelow29(assetsPath)
        } else {
            copyToDownloadsApi29(assetsPath)
        }
    }

    protected fun sendHelloMessageFromUser() {
        ChatMainScreen {
            inputEditView { isVisible() }
            welcomeScreen { isVisible() }

            inputEditView.typeText(helloTextToSend)
            sendMessageBtn {
                isVisible()
                click()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun copyToDownloadsApiBelow29(filePathRelativeToAssets: String): String? {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.absolutePath?.let {
            val fileName = filePathRelativeToAssets.split("/").last()
            val toFile = File("$it/$fileName")
            if (toFile.exists() && toFile.length() > 0) {
                return fileName
            } else if (toFile.exists()) {
                toFile.delete()
            }
            context.assets.open(filePathRelativeToAssets).toFile(toFile.createFileIfNeeded())
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            dm.addCompletedDownload(
                fileName,
                BaseConfig.getInstance().context.getString(R.string.ecc_media_description),
                true,
                getMimeType(fileName),
                toFile.path,
                toFile.length(),
                false
            )
            return fileName
        }

        return null
    }

    /**
     * Возвращает newFileName, если в процессе копирования файла
     * случилась ошибка UNIQUE constraint failed
     */
    private fun copyToDownloadsApi29(filePathRelativeToAssets: String, nameOfFile: String? = null): String? {
        try {
            val fileName = nameOfFile ?: getNameOfFile(filePathRelativeToAssets)
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.absolutePath?.let {
                val toFile = File("$it/$fileName")
                if (toFile.exists() && toFile.length() > 0) {
                    return null
                } else if (toFile.exists()) {
                    toFile.delete()
                }
            }
            val values = ContentValues()
            values.put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                fileName
            )
            values.put(
                MediaStore.MediaColumns.MIME_TYPE,
                getMimeType(fileName)
            )
            values.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS
            )
            val uri: Uri? = context.contentResolver.insert(
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                values
            )
            context.assets.open(filePathRelativeToAssets).copyToUri(uri!!, context)
            return fileName
        } catch (exc: android.database.sqlite.SQLiteConstraintException) {
            val name = getNameOfFile(filePathRelativeToAssets, true)
            copyToDownloadsApi29(filePathRelativeToAssets, name)
            return name
        }
    }

    private fun getNameOfFile(filePathRelativeToAssets: String, plusRandom: Boolean = false): String {
        val usualName = filePathRelativeToAssets.split("/").last()
        return if (plusRandom) {
            val nameParts = usualName.split(".")
            "${nameParts[0]}$userId.${nameParts[1]}"
        } else {
            usualName
        }
    }

    private fun getMimeType(fileName: String): String {
        return if (fileName.endsWith(".jpg")) {
            "image/jpeg"
        } else if (fileName.endsWith(".jpeg")) {
            "image/pjpeg"
        } else if (fileName.endsWith(".png")) {
            "image/png"
        } else if (fileName.endsWith(".gif")) {
            "image/gif"
        } else if (fileName.endsWith(".tiff")) {
            "image/tiff"
        } else if (fileName.endsWith(".ogg")) {
            "audio/ogg"
        } else if (fileName.endsWith(".aac")) {
            "image/aac"
        } else if (fileName.endsWith(".pdf")) {
            "application/pdf"
        } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            "application/msword"
        } else if (fileName.endsWith(".zip")) {
            "application/zip"
        } else if (fileName.endsWith(".gzip")) {
            "application/gzip"
        } else if (fileName.endsWith(".xml")) {
            "application/xml"
        } else {
            "*/*"
        }
    }

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
        isShowMenu = true
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
