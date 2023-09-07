package io.edna.threads.demo.mainChatScreen.errors

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import im.threads.BuildConfig
import im.threads.ui.activities.ChatActivity
import io.edna.threads.demo.BaseTestCase
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebSocketEnterFlowTest : BaseTestCase() {
    private val intent = Intent(ApplicationProvider.getApplicationContext(), ChatActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<ChatActivity>(intent)

    init {
        applyServerSettings()
        initUserDirectly()
        prepareMocks(HashMap())
        BuildConfig.IS_ANIMATIONS_DISABLED.set(true)
    }

    /*@Test
    fun testNoRegisterDevice() {
        ChatMainScreen {
            errorImage { isVisible() }
            errorText {
                isVisible()
                containsText("register")
            }
            errorRetryBtn { isVisible() }
        }
    }*/
}
