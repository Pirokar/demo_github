package io.edna.threads.demo.mainChatScreen.errors

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import im.threads.BuildConfig
import io.edna.threads.demo.BaseTestCase
import io.edna.threads.demo.appCode.activity.MainActivity
import io.edna.threads.demo.assert
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import io.edna.threads.demo.waitForExists
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
