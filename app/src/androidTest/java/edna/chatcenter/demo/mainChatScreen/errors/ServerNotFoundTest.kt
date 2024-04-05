package edna.chatcenter.demo.mainChatScreen.errors

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import edna.chatcenter.demo.BaseTestCase
import edna.chatcenter.demo.appCode.activity.MainActivity
import edna.chatcenter.demo.assert
import edna.chatcenter.demo.kaspressoSreens.ChatMainScreen
import edna.chatcenter.demo.waitForExists
import edna.chatcenter.ui.BuildConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ServerNotFoundTest : BaseTestCase() {
    private val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<MainActivity>(intent)

    init {
        applyDefaultUserToDemoApp()
        BuildConfig.IS_ANIMATIONS_DISABLED.set(true)
    }

    @Test
    fun testServerNotFound() {
        openChatFromDemoLoginPage()
        Thread.sleep(config.networkConfig.httpConfig.connectionTimeout * 1000L)
        ChatMainScreen {
            errorImage.waitForExists(60000)
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
}
