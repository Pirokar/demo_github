package edna.chatcenter.demo.mainChatScreen.errors

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import edna.chatcenter.demo.BaseTestCase
import edna.chatcenter.demo.TestMessages
import edna.chatcenter.demo.TestMessages.defaultConfigNoAttachmentSettingsMock
import edna.chatcenter.demo.TestMessages.defaultConfigNoSettingsMock
import edna.chatcenter.demo.appCode.activity.MainActivity
import edna.chatcenter.demo.assert
import edna.chatcenter.demo.getText
import edna.chatcenter.demo.kaspressoSreens.ChatMainScreen
import edna.chatcenter.demo.waitForExists
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebSocketEnterFlowTest : BaseTestCase() {
    private val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<MainActivity>(intent)

    init {
        applyDefaultUserToDemoApp()
        prepareWsMocks()
    }

    @Test
    fun testNoRegisterDevice() {
        prepareHttpMocks()
        wsMocksMap = HashMap()
        openChatFromDemoLoginPage()
        ChatMainScreen {
            errorImage.waitForExists(getSocketTimeout() + 1000L)
            errorImage {
                assert("Изображение с ошибкой должно быть видимо") { isVisible() }
            }
            errorText {
                assert("Текст с ошибкой должен быть видимый") { isVisible() }
                assert("Текст с ошибкой должен содержать: \"REGISTER\"") {
                    containsText("REGISTER")
                }
            }
            errorRetryBtn {
                assert("Кнопка повтора загрузки списка сообщений быть видимой") { isVisible() }
            }
        }
    }

    @Test
    fun testNoInitChat() {
        wsMocksMap = HashMap<String, String>().apply {
            put("registerDevice", TestMessages.registerDeviceWsAnswer)
        }
        openChatFromDemoLoginPage()

        ChatMainScreen {
            errorImage.waitForExists(getSocketTimeout() + 1000L)
            errorImage {
                assert("Изображение с ошибкой должно быть видимо") { isVisible() }
            }
            errorText {
                assert("Текст с ошибкой должен быть видимый") { isVisible() }
                assert("Текст с ошибкой не должен быть пустым") { hasAnyText() }
            }
            errorRetryBtn {
                assert("Кнопка повтора загрузки списка сообщений быть видимой") { isVisible() }
            }
        }
    }

    @Test
    fun testNoClientInfo() {
        wsMocksMap = HashMap<String, String>().apply {
            put("registerDevice", TestMessages.registerDeviceWsAnswer)
            put("INIT_CHAT", TestMessages.initChatWsAnswer)
        }
        openChatFromDemoLoginPage()

        ChatMainScreen {
            errorImage.waitForExists(getSocketTimeout() + 1000L)
            errorImage {
                assert("Изображение с ошибкой должно быть видимо") { isVisible() }
            }
            errorText {
                assert("Текст с ошибкой должен быть видимый и не должен быть пустым", ::isVisible, ::hasAnyText)
            }
            errorRetryBtn {
                assert("Кнопка повтора загрузки списка сообщений быть видимой") { isVisible() }
            }
        }
    }

    @Test
    fun testNoSettings() {
        wsMocksMap = HashMap()
        prepareHttpMocks(configAnswer = defaultConfigNoSettingsMock)
        openChatFromDemoLoginPage()
        ChatMainScreen {
            errorImage.waitForExists(getSocketTimeout() + 1000L)
            errorImage {
                assert("Изображение с ошибкой должно быть видимо") { isVisible() }
            }
            errorText {
                assert("Текст с ошибкой должен быть видимый") { isVisible() }
                assert("Текст ошибки должен быть видим") { hasAnyText() }
            }
            errorRetryBtn {
                assert("Кнопка повтора загрузки списка сообщений быть видимой") { isVisible() }
            }
        }
    }

    @Test
    fun testNoAttachmentSettings() {
        wsMocksMap = HashMap()
        prepareHttpMocks(configAnswer = defaultConfigNoAttachmentSettingsMock)
        openChatFromDemoLoginPage()
        ChatMainScreen {
            errorImage.waitForExists(getSocketTimeout() + 1000L)
            errorImage {
                assert("Изображение с ошибкой должно быть видимо") { isVisible() }
            }

            errorText {
                val text = getText(view.interaction)
                assert("Текст с ошибкой должен быть видимый") { isVisible() }
                assert("Текст ошибки должен быть видим") { hasAnyText() }
            }
            errorRetryBtn {
                assert("Кнопка повтора загрузки списка сообщений быть видимой") { isVisible() }
            }
        }
    }
}
