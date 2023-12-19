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
import io.edna.threads.demo.assert
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
        val uiRecordButton = uiDevice.findObject(uiSelector)
        ChatMainScreen {
            assert("Кнопка записи должна отображаться и быть активной") {
                recordButton.isVisible()
            }
            uiRecordButton.longClick()
            uiRecordButton.waitForExists(100)
            assert("Кнопка \"Play/Stop\" должна отображаться") {
                playPauseButton.isVisible()
            }
            assert("Прогресс бар для аудиофайла должен отображаться") {
                quoteSlider.isVisible()
            }
            assert("Кнопка \"Удалить вложение\" должна  отображаться") {
                quoteClear.isVisible()
                quoteClear.click()
            }
            assert("Лейаут с вложением не должен отображаться") {
                playPauseButton.isNotDisplayed()
                quoteSlider.isNotDisplayed()
                quoteClear.isNotDisplayed()
            }
            assert("Кнопка записи должна отображаться и быть активной") {
                recordButton.isVisible()
            }
        }
    }

    @Test
    fun prepareAndRemoveVoiceMessageNoHistoryTest() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        val uiRecordButton = uiDevice.findObject(uiSelector)
        ChatMainScreen {
            assert("Кнопка записи должна отображаться и быть активной") {
                recordButton.isVisible()
                uiRecordButton.longClick()
                uiRecordButton.waitForExists(100)
            }
            assert("Кнопка \"Play/Stop\" должна отображаться") {
                playPauseButton.isVisible()
            }
            assert("Прогресс бар для аудиофайла должен отображаться") {
                quoteSlider.isVisible()
            }
            assert("Кнопка \"Удалить вложение\" должна  отображаться") {
                quoteClear.isVisible()
                quoteClear.click()
            }
            assert("Лейаут с вложением не должен отображаться") {
                playPauseButton.isNotDisplayed()
                quoteSlider.isNotDisplayed()
                quoteClear.isNotDisplayed()
            }
            assert("Кнопка записи должна отображаться и быть активной") {
                recordButton.isVisible()
            }
        }
    }

    @Test
    fun prepareAndSendVoiceMessageWithPlayPreviewTest() {
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_files_response))
        openChatFromDemoLoginPage()
        val recordButton = uiDevice.findObject(uiSelector)
        ChatMainScreen {
            assert("Кнопка записи должна отображаться и быть активной") {
                ChatMainScreen.recordButton.isVisible()
                recordButton.longClick()
                recordButton.waitForExists(100)
            }
            assert("Прогресс бар для аудиофайла должен отображаться") {
                quoteSlider.isVisible()
            }
            assert("Кнопка \"Удалить вложение\" должна  отображаться") {
                quoteClear.isVisible()
            }
            assert("Кнопка \"Play/Stop\" должна отображаться") {
                playPauseButton.isVisible()
                playPauseButton.click()
                recordButton.waitForExists(4000)
            }
            val sizeBeforeSend = chatItemsRecyclerView.getSize()
            assert("Кнопка \"Отправить сообщение\" должна  отображаться") {
                sendMessageBtn.isVisible()
                sendMessageBtn.click()
            }
            assert("В списке должно отображаться больше сообщений, чем до отправки") {
                assert(chatItemsRecyclerView.getSize() > sizeBeforeSend)
            }
            assert("В списке сообщений должно быть аудиосообщение от пользователя") {
                chatItemsRecyclerView {
                    isVisible()
                    scrollTo(0)
                }
            }
        }
    }

    @Test
    fun prepareAndSendVoiceMessageNoHistoryWithDeleteMessageTest() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        val recordButton = uiDevice.findObject(uiSelector)
        ChatMainScreen {
            assert("Кнопка записи должна отображаться и быть активной") {
                ChatMainScreen.recordButton.isVisible()
                recordButton.longClick()
                recordButton.waitForExists(100)
            }
            assert("Кнопка \"Play/Stop\" должна отображаться") {
                playPauseButton.isVisible()
            }
            assert("Прогресс бар для аудиофайла должен отображаться") {
                quoteSlider.isVisible()
            }
            assert("Кнопка \"Удалить вложение\" должна  отображаться") {
                quoteClear.isVisible()
            }

            val sizeBeforeSend = chatItemsRecyclerView.getSize()
            assert("Кнопка \"Отправить сообщение\" должна  отображаться") {
                sendMessageBtn.isVisible()
                sendMessageBtn.click()
            }
            assert("В списке должно отображаться ${sizeBeforeSend + 2} сообщений") {
                assert(chatItemsRecyclerView.getSize() == sizeBeforeSend + 2)
            }
            assert("В списке сообщений должно быть аудиосообщение от пользователя") {
                chatItemsRecyclerView {
                    isVisible()
                    scrollTo(0)
                    lastChild<ChatMainScreen.ChatRecyclerItem> {
                        click()
                    }
                }
            }
            assert("Должно отображаться меню \"Удалить/Попробовать снова\"") {
                onView(withText("Delete")).perform(click())
            }
            assert("В списке должно отображаться ${sizeBeforeSend + 1} сообщений") {
                assert(chatItemsRecyclerView.getSize() == sizeBeforeSend + 1)
            }
        }
    }
}
