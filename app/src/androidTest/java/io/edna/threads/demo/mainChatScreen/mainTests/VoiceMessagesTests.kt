package io.edna.threads.demo.mainChatScreen.mainTests

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import io.edna.threads.demo.BaseFilesTestCase
import io.edna.threads.demo.R
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VoiceMessagesTests : BaseFilesTestCase() {

    @Rule
    @JvmField
    val audioPermissionRule = GrantPermissionRule.grant(
        "android.permission.RECORD_AUDIO"
    )!!

    private val uiSelector = UiSelector().resourceId("io.edna.threads.demo:id/record_button")
    private lateinit var uiDevice: UiDevice

    @Before
    fun before() {
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.executeShellCommand("settings put secure long_press_timeout 3000")
    }

    @Test
    fun prepareAndRemoveVoiceMessageTest() {
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_files_response))
        openChatFromDemoLoginPage()
        val recordButton = uiDevice.findObject(uiSelector)

        ChatMainScreen.recordButton {
            isVisible()
            recordButton.longClick()
            recordButton.waitForExists(100)
        }

        ChatMainScreen.playPauseButton { isVisible() }
        ChatMainScreen.quoteSlider { isVisible() }
        ChatMainScreen.quoteClear {
            isVisible()
            click()
        }

        ChatMainScreen.playPauseButton { isNotDisplayed() }
        ChatMainScreen.quoteSlider { isNotDisplayed() }
        ChatMainScreen.quoteClear { isNotDisplayed() }
        ChatMainScreen.recordButton { isVisible() }
    }

    @Test
    fun prepareAndRemoveVoiceMessageNoHistoryTest() {
        openChatFromDemoLoginPage()
        val recordButton = uiDevice.findObject(uiSelector)

        ChatMainScreen.recordButton {
            isVisible()
            recordButton.longClick()
            recordButton.waitForExists(100)
        }

        ChatMainScreen.playPauseButton { isVisible() }
        ChatMainScreen.quoteSlider { isVisible() }
        ChatMainScreen.quoteClear {
            isVisible()
            click()
        }

        ChatMainScreen.playPauseButton { isNotDisplayed() }
        ChatMainScreen.quoteSlider { isNotDisplayed() }
        ChatMainScreen.quoteClear { isNotDisplayed() }
        ChatMainScreen.recordButton { isVisible() }
    }

    @Test
    fun prepareAndSendVoiceMessageWithPlayTest() {
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_files_response))
        openChatFromDemoLoginPage()
        val recordButton = uiDevice.findObject(uiSelector)

        ChatMainScreen.recordButton {
            isVisible()
            recordButton.longClick()
            recordButton.waitForExists(100)
        }

        ChatMainScreen.playPauseButton { isVisible() }
        ChatMainScreen.quoteSlider { isVisible() }
        ChatMainScreen.quoteClear { isVisible() }

        val sizeBeforeSend = ChatMainScreen.chatItemsRecyclerView.getSize()
        ChatMainScreen.sendMessageBtn {
            isVisible()
            click()
            recordButton.waitForExists(1000)
        }
        assert(ChatMainScreen.chatItemsRecyclerView.getSize() == sizeBeforeSend + 2)
        ChatMainScreen {
            chatItemsRecyclerView {
                isVisible()
                scrollTo(0)
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    click()
                }
                recordButton.waitForExists(3000)
            }
        }
        assert(ChatMainScreen.chatItemsRecyclerView.getSize() == sizeBeforeSend + 2)
    }

    @Test
    fun prepareAndSendVoiceMessageNoHistoryWithDeleteMessageTest() {
        openChatFromDemoLoginPage()
        val recordButton = uiDevice.findObject(uiSelector)

        ChatMainScreen.recordButton {
            isVisible()
            recordButton.longClick()
            recordButton.waitForExists(100)
        }

        ChatMainScreen.playPauseButton { isVisible() }
        ChatMainScreen.quoteSlider { isVisible() }
        ChatMainScreen.quoteClear { isVisible() }

        val sizeBeforeSend = ChatMainScreen.chatItemsRecyclerView.getSize()
        ChatMainScreen.sendMessageBtn {
            isVisible()
            click()
        }
        assert(ChatMainScreen.chatItemsRecyclerView.getSize() == sizeBeforeSend + 2)
        ChatMainScreen {
            chatItemsRecyclerView {
                isVisible()
                scrollTo(0)
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    click()
                }
            }
            onView(withText("Delete")).perform(click())
        }
        assert(ChatMainScreen.chatItemsRecyclerView.getSize() == sizeBeforeSend + 1)
    }
}
