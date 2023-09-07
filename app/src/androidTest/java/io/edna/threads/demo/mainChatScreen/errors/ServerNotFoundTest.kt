package io.edna.threads.demo.mainChatScreen.errors

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import im.threads.BuildConfig
import im.threads.ui.activities.ChatActivity
import io.edna.threads.demo.TestCaseWithLoginInfo
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ServerNotFoundTest : TestCaseWithLoginInfo() {
    private val intent = Intent(ApplicationProvider.getApplicationContext(), ChatActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<ChatActivity>(intent)

    private val urlPath = "wrongednalink"
    private val wrongLink = "http://$urlPath.ru"

    init {
        applyServerSettings(wrongLink, wrongLink, wrongLink)
        initUserDirectly()
        BuildConfig.IS_ANIMATIONS_DISABLED.set(true)
    }

    @Test
    fun testServerNotFound() {
        ChatMainScreen {
            errorImage { isVisible() }
            errorText {
                isVisible()
                containsText(urlPath)
            }
            errorRetryBtn { isVisible() }
        }
    }
}
