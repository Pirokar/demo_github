package io.edna.threads.demo.mainChatScreen.firstLogin

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import im.threads.ui.activities.ChatActivity
import io.edna.threads.demo.TestCaseWithLoginInfo
import io.edna.threads.demo.mainChatScreen.ChatMainScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirstLoginTest : TestCaseWithLoginInfo() {
    private val intent = Intent(ApplicationProvider.getApplicationContext(), ChatActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<ChatActivity>(intent)

    init {
        prepareMocks()
        initUserDirectly()
    }

    @Test
    fun firstLogin() {
        val textToSend = "Hello, Edna! This is a test message"
        ChatMainScreen {
            inputEditView { isVisible() }
            welcomeScreen { isVisible() }

            inputEditView.typeText(textToSend)
            sendMessageBtn {
                isVisible()
                click()
            }
            recyclerView {
                isVisible()
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    itemText.containsText(textToSend)
                }
            }
        }
    }
}
