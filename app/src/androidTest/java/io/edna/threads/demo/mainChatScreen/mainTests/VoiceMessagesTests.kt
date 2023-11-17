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
import org.junit.After
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
        uiDevice.executeShellCommand("settings put secure long_press_timeout 4000")
    }

    @After
    override fun after() {
        super.after()
        uiDevice.executeShellCommand("settings put secure long_press_timeout 2000")
    }

    @Test
    fun prepareAndRemoveVoiceMessageTest() {
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_files_response))
        openChatFromDemoLoginPage()
        val recordButton = uiDevice.findObject(uiSelector)
        io.edna.threads.demo.assert("Кнопка записи должна отображаться и быть активной") {
            ChatMainScreen.recordButton.isVisible()
            recordButton.longClick()
            recordButton.waitForExists(100)
        }
        io.edna.threads.demo.assert("Кнопка \"Play/Stop\" должна отображаться") {
            ChatMainScreen.playPauseButton.isVisible()
        }
        io.edna.threads.demo.assert("Прогресс бар для аудиофайла должен отображаться") {
            ChatMainScreen.quoteSlider.isVisible()
        }
        io.edna.threads.demo.assert("Кнопка \"Удалить вложение\" должна  отображаться") {
            ChatMainScreen.quoteClear.isVisible()
            ChatMainScreen.quoteClear.click()
        }
        io.edna.threads.demo.assert("Лейаут с вложением не должен отображаться") {
            ChatMainScreen.playPauseButton.isNotDisplayed()
            ChatMainScreen.quoteSlider.isNotDisplayed()
            ChatMainScreen.quoteClear.isNotDisplayed()
        }
        io.edna.threads.demo.assert("Кнопка записи должна отображаться и быть активной") {
            ChatMainScreen.recordButton.isVisible()
        }
    }

    @Test
    fun prepareAndRemoveVoiceMessageNoHistoryTest() {
        openChatFromDemoLoginPage()
        val recordButton = uiDevice.findObject(uiSelector)

        io.edna.threads.demo.assert("Кнопка записи должна отображаться и быть активной") {
            ChatMainScreen.recordButton.isVisible()
            recordButton.longClick()
            recordButton.waitForExists(100)
        }
        io.edna.threads.demo.assert("Кнопка \"Play/Stop\" должна отображаться") {
            ChatMainScreen.playPauseButton.isVisible()
        }
        io.edna.threads.demo.assert("Прогресс бар для аудиофайла должен отображаться") {
            ChatMainScreen.quoteSlider.isVisible()
        }
        io.edna.threads.demo.assert("Кнопка \"Удалить вложение\" должна  отображаться") {
            ChatMainScreen.quoteClear.isVisible()
            ChatMainScreen.quoteClear.click()
        }
        io.edna.threads.demo.assert("Лейаут с вложением не должен отображаться") {
            ChatMainScreen.playPauseButton.isNotDisplayed()
            ChatMainScreen.quoteSlider.isNotDisplayed()
            ChatMainScreen.quoteClear.isNotDisplayed()
        }
        io.edna.threads.demo.assert("Кнопка записи должна отображаться и быть активной") {
            ChatMainScreen.recordButton.isVisible()
        }
    }

    @Test
    fun prepareAndSendVoiceMessageWithPlayPreviewTest() {
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_files_response))
        openChatFromDemoLoginPage()
        val recordButton = uiDevice.findObject(uiSelector)

        io.edna.threads.demo.assert("Кнопка записи должна отображаться и быть активной") {
            ChatMainScreen.recordButton.isVisible()
            recordButton.longClick()
            recordButton.waitForExists(100)
        }
        io.edna.threads.demo.assert("Прогресс бар для аудиофайла должен отображаться") {
            ChatMainScreen.quoteSlider.isVisible()
        }
        io.edna.threads.demo.assert("Кнопка \"Удалить вложение\" должна  отображаться") {
            ChatMainScreen.quoteClear.isVisible()
        }
        io.edna.threads.demo.assert("Кнопка \"Play/Stop\" должна отображаться") {
            ChatMainScreen.playPauseButton.isVisible()
            ChatMainScreen.playPauseButton.click()
            recordButton.waitForExists(4000)
        }
        val sizeBeforeSend = ChatMainScreen.chatItemsRecyclerView.getSize()
        io.edna.threads.demo.assert("Кнопка \"Отправить сообщение\" должна  отображаться") {
            ChatMainScreen.sendMessageBtn.isVisible()
            ChatMainScreen.sendMessageBtn.click()
        }
        io.edna.threads.demo.assert("В списке должно отображаться ${sizeBeforeSend + 2} сообщений") {
            assert(ChatMainScreen.chatItemsRecyclerView.getSize() == sizeBeforeSend + 2)
        }
        io.edna.threads.demo.assert("В списке сообщений должно быть аудиосообщение от пользователя") {
            ChatMainScreen {
                chatItemsRecyclerView {
                    isVisible()
                    scrollTo(0)
                }
            }
        }
        io.edna.threads.demo.assert("В списке должно отображаться ${sizeBeforeSend + 2} сообщений") {
            assert(ChatMainScreen.chatItemsRecyclerView.getSize() == sizeBeforeSend + 2)
        }
    }

    @Test
    fun prepareAndSendVoiceMessageNoHistoryWithDeleteMessageTest() {
        openChatFromDemoLoginPage()
        val recordButton = uiDevice.findObject(uiSelector)

        io.edna.threads.demo.assert("Кнопка записи должна отображаться и быть активной") {
            ChatMainScreen.recordButton.isVisible()
            recordButton.longClick()
            recordButton.waitForExists(100)
        }
        io.edna.threads.demo.assert("Кнопка \"Play/Stop\" должна отображаться") {
            ChatMainScreen.playPauseButton.isVisible()
        }
        io.edna.threads.demo.assert("Прогресс бар для аудиофайла должен отображаться") {
            ChatMainScreen.quoteSlider.isVisible()
        }
        io.edna.threads.demo.assert("Кнопка \"Удалить вложение\" должна  отображаться") {
            ChatMainScreen.quoteClear.isVisible()
        }

        val sizeBeforeSend = ChatMainScreen.chatItemsRecyclerView.getSize()
        io.edna.threads.demo.assert("Кнопка \"Отправить сообщение\" должна  отображаться") {
            ChatMainScreen.sendMessageBtn.isVisible()
            ChatMainScreen.sendMessageBtn.click()
        }
        io.edna.threads.demo.assert("В списке должно отображаться ${sizeBeforeSend + 2} сообщений") {
            assert(ChatMainScreen.chatItemsRecyclerView.getSize() == sizeBeforeSend + 2)
        }
        io.edna.threads.demo.assert("В списке сообщений должно быть аудиосообщение от пользователя") {
            ChatMainScreen {
                chatItemsRecyclerView {
                    isVisible()
                    scrollTo(0)
                    lastChild<ChatMainScreen.ChatRecyclerItem> {
                        click()
                    }
                }
            }
        }
        io.edna.threads.demo.assert("Должно отображатся меню \"Удалить/Попробовать снова\"") {
            onView(withText("Delete")).perform(click())
        }
        io.edna.threads.demo.assert("В списке должно отображаться ${sizeBeforeSend + 1} сообщений") {
            assert(ChatMainScreen.chatItemsRecyclerView.getSize() == sizeBeforeSend + 1)
        }
    }
}
