package io.edna.threads.demo.mainChatScreen.firstLogin

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import im.threads.BuildConfig
import im.threads.ui.activities.ChatActivity
import io.edna.threads.demo.TestCaseWithLoginInfo
import io.edna.threads.demo.mainChatScreen.ChatMainScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgressBarTest : TestCaseWithLoginInfo() {
    private val intent = Intent(ApplicationProvider.getApplicationContext(), ChatActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<ChatActivity>(intent)

    init {
        BuildConfig.IS_ANIMATIONS_DISABLED.set(true)
        prepareMocks()
        initUserDirectly()
    }

    @Test
    fun progressbarOnStart() {
        ChatMainScreen {
            progressBar { isVisible() }
        }
    }
}
