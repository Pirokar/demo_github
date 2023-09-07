package io.edna.threads.demo.mainChatScreen.errors

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import im.threads.BuildConfig
import im.threads.R
import im.threads.ui.activities.ChatActivity
import io.edna.threads.demo.TestCaseWithLoginInfo
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoUserTest : TestCaseWithLoginInfo() {
    private val intent = Intent(ApplicationProvider.getApplicationContext(), ChatActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<ChatActivity>(intent)

    init {
        BuildConfig.IS_ANIMATIONS_DISABLED.set(true)
        prepareMocks()
    }

    @Test
    fun testNoUser() {
        ChatMainScreen {
            errorImage { isVisible() }
            errorText {
                isVisible()
                hasText(context.getString(R.string.ecc_no_user_id))
            }
            errorRetryBtn { isVisible() }
        }
    }
}
