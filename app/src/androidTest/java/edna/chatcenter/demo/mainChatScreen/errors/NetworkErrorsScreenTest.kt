package edna.chatcenter.demo.mainChatScreen.errors

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import edna.chatcenter.demo.BaseTestCase
import edna.chatcenter.demo.TestMessages
import edna.chatcenter.demo.appCode.activity.MainActivity
import edna.chatcenter.demo.assert
import edna.chatcenter.demo.kaspressoSreens.ChatMainScreen
import edna.chatcenter.ui.R
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
