package io.edna.threads.demo.mainChatScreen.firstLogin

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.edna.threads.demo.BaseTestCase
import io.edna.threads.demo.appCode.activity.MainActivity
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirstLoginTest : BaseTestCase() {
    private val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<MainActivity>(intent)

    init {
        applyDefaultUserToDemoApp()
        prepareWsMocks()
    }

    @Test
    fun firstLogin() {
        prepareHttpMocks()
        val textToSend = "Hello, Edna! This is a test message"

        openChatFromDemoLoginPage()
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

    @Test
    fun progressbarOnStart() {
        prepareHttpMocks(9000)
        openChatFromDemoLoginPage()
        ChatMainScreen {
            progressBar { isVisible() }
        }
    }
}
