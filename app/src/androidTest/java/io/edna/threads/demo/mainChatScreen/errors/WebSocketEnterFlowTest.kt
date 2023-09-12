package io.edna.threads.demo.mainChatScreen.errors

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import im.threads.business.config.BaseConfig
import io.edna.threads.demo.BaseTestCase
import io.edna.threads.demo.appCode.activity.MainActivity
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebSocketEnterFlowTest : BaseTestCase() {
    private val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<MainActivity>(intent)

    init {
        applyServerSettings()
        applyDefaultUserToDemoApp()
        prepareWsMocks(HashMap())
    }

    @Test
    fun testNoRegisterDevice() {
        openChatFromDemoLoginPage()
        Thread.sleep(BaseConfig.getInstance().requestConfig.socketClientSettings.connectTimeoutMillis)
        ChatMainScreen {
            errorImage { isVisible() }
            errorText {
                isVisible()
                hasAnyText()
            }
            errorRetryBtn { isVisible() }
        }
    }
}
