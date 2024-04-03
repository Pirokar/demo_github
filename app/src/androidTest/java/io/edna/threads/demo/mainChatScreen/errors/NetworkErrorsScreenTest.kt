package io.edna.threads.demo.mainChatScreen.errors

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import im.threads.BuildConfig
import im.threads.R
import io.edna.threads.demo.BaseTestCase
import io.edna.threads.demo.TestMessages
import io.edna.threads.demo.appCode.activity.MainActivity
import io.edna.threads.demo.assert
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.UnknownHostException

@RunWith(AndroidJUnit4::class)
class NetworkErrorsScreenTest : BaseTestCase() {
    private val intent =
        Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<MainActivity>(intent)

    init {
        applyDefaultUserToDemoApp()
        BuildConfig.IS_ANIMATIONS_DISABLED.set(true)
    }

    @Test
    fun testHTTPNetworkErrorCodes() {
        prepareWsMocks()
        prepareHttpErrorMocks()
        openChatFromDemoLoginPage()
        ChatMainScreen {
            errorImage {
                assert("Изображение с ошибкой должно быть видимо") { isVisible() }
            }
            errorText {
                assert("Текст с ошибкой должен быть видимым", ::isVisible)
                assert("Текст с ошибкой должен содержать текст: \"${context.getString(R.string.ecc_chat_not_available)}\"") {
                    hasText(context.getString(R.string.ecc_chat_not_available))
                }
            }
            errorRetryBtn {
                assert("Кнопка Повторить должна быть видимой") { isVisible() }
            }
        }
    }

    @Test
    fun testSocketNetworkErrorCodes() {
        prepareHttpMocks()
        prepareWsMocks(UnknownHostException(TestMessages.websocketErrorStringMock))
        openChatFromDemoLoginPage()
        ChatMainScreen {
            errorImage {
                assert("Изображение с ошибкой должно быть видимо") { isVisible() }
            }
            errorText {
                assert("Текст с ошибкой должен быть видимым", ::isVisible)
                assert("Текст с ошибкой должен содержать текст: \"${context.getString(R.string.ecc_chat_not_available)}\"") {
                    hasText(context.getString(R.string.ecc_chat_not_available))
                }
            }
            errorRetryBtn {
                assert("Кнопка Повторить должна быть видимой") { isVisible() }
            }
        }
    }
}