package io.edna.threads.demo.mainChatScreen.errors

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.edna.threads.demo.BaseTestCase
import io.edna.threads.demo.TestMessages
import io.edna.threads.demo.TestMessages.defaultConfigNoAttachmentSettingsMock
import io.edna.threads.demo.TestMessages.defaultConfigNoSettingsMock
import io.edna.threads.demo.appCode.activity.MainActivity
import io.edna.threads.demo.assert
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import io.edna.threads.demo.waitForExists
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
            errorImage.waitForExists(60000)
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
        Thread.sleep(config.networkConfig.httpConfig.connectionTimeout * 1000L)
        ChatMainScreen {
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
        Thread.sleep(config.networkConfig.httpConfig.connectionTimeout * 1000L)
        ChatMainScreen {
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
        Thread.sleep(config.networkConfig.httpConfig.connectionTimeout * 1000L)
        ChatMainScreen {
            errorImage {
                assert("Изображение с ошибкой должно быть видимо") { isVisible() }
            }
            errorText {
                assert("Текст с ошибкой должен быть видимый") { isVisible() }
                assert("Текст с ошибкой должен содержать: \"${context.getString(im.threads.R.string.ecc_settings_not_loaded)}\"") {
                    hasText(context.getString(im.threads.R.string.ecc_settings_not_loaded))
                }
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
        Thread.sleep(config.networkConfig.httpConfig.connectionTimeout * 1000L)
        ChatMainScreen {
            errorImage {
                assert("Изображение с ошибкой должно быть видимо") { isVisible() }
            }
            errorText {
                assert("Текст с ошибкой должен быть видимый") { isVisible() }
                assert("Текст с ошибкой должен содержать: \"${context.getString(im.threads.R.string.ecc_attachment_settings_not_loaded)}\"") {
                    hasText(context.getString(im.threads.R.string.ecc_attachment_settings_not_loaded))
                }
            }
            errorRetryBtn {
                assert("Кнопка повтора загрузки списка сообщений быть видимой") { isVisible() }
            }
        }
    }
}
