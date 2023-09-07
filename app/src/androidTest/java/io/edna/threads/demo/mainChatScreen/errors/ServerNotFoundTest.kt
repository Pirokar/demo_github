package io.edna.threads.demo.mainChatScreen.errors

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import im.threads.BuildConfig
import im.threads.ui.activities.ChatActivity
import io.edna.threads.demo.BaseTestCase
import io.edna.threads.demo.appCode.activity.MainActivity
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
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
        ChatMainScreen {
            errorImage { isVisible() }
            errorText {
                isVisible()
                containsText(localhostUrl)
            }
            errorRetryBtn { isVisible() }
        }
    }
}
