package edna.chatcenter.demo.mainChatScreen.mainTests

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import edna.chatcenter.demo.BaseTestCase
import edna.chatcenter.demo.TestMessages.defaultConfigNoScheduleMock
import edna.chatcenter.demo.TestMessages.defaultConfigScheduleInactiveCanSendMock
import edna.chatcenter.demo.TestMessages.defaultConfigScheduleInactiveCannotSendMock
import edna.chatcenter.demo.TestMessages.emptyNoThreadHistoryMessage
import edna.chatcenter.demo.TestMessages.operatorHelloMessage
import edna.chatcenter.demo.appCode.activity.MainActivity
import edna.chatcenter.demo.assert
import edna.chatcenter.demo.kaspressoSreens.ChatMainScreen
import edna.chatcenter.demo.waitForExists
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotWorkingMessagesTests : BaseTestCase() {

    private val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<MainActivity>(intent)

    private val textToSend = "Hello, Edna! This is test message for test schedule."
    private val scheduleNotificationMessage = "Война войной, а обед по расписанию."
    private val operatorHelloMessageText = "привет!"

    init {
        applyDefaultUserToDemoApp()
    }

    @Test
    fun sendMessageNoClientInfoResponse() {
        openChatFromDemoLoginPage()
        ChatMainScreen.errorImage.waitForExists(getSocketTimeout() + 1000)
        checkInputFieldsNoClientInfoResponse()
    }

    @Test
    fun sendMessageNoSchedule() {
        prepareHttpMocks(configAnswer = defaultConfigNoScheduleMock)
        prepareWsMocks()
        openChatFromDemoLoginPage()
        checkSendMessageFromClient()
    }

    @Test
    fun sendMessageWhenScheduleActive() {
        prepareHttpMocks()
        prepareWsMocks()
        openChatFromDemoLoginPage()
        checkSendMessageFromClient()
    }

    @Test
    fun sendMessageWhenScheduleInActiveSendDuringInactive() {
        prepareHttpMocks(
            configAnswer = defaultConfigScheduleInactiveCanSendMock,
            historyAnswer = emptyNoThreadHistoryMessage
        )
        prepareWsMocks()
        openChatFromDemoLoginPage()
        checkInputFieldsEnabledHasNotificationMessage()
    }

    @Test
    fun whenScheduleIsActive_sendMessageFromConsult_duringInactive() {
        prepareHttpMocks(
            configAnswer = defaultConfigScheduleInactiveCanSendMock,
            historyAnswer = emptyNoThreadHistoryMessage
        )
        prepareWsMocks()
        openChatFromDemoLoginPage()
        checkInputFieldsEnabledAndHasNotificationMessageWithMessageFromConsultant()
    }

    @Test
    fun sendMessageWhenScheduleInActiveNotSendDuringInactive() {
        prepareHttpMocks(
            configAnswer = defaultConfigScheduleInactiveCannotSendMock,
            historyAnswer = emptyNoThreadHistoryMessage
        )
        prepareWsMocks()
        openChatFromDemoLoginPage()
        checkInputFieldsDisabledHasNotificationMessage()
    }

    @Test
    fun sendMessageWhenScheduleInActiveNotSendDuringInactiveWithMessageFromConsultant() {
        prepareHttpMocks(
            configAnswer = defaultConfigScheduleInactiveCannotSendMock,
            historyAnswer = emptyNoThreadHistoryMessage
        )
        prepareWsMocks()
        openChatFromDemoLoginPage()
        checkInputFieldsDisabledHasNotificationMessageWithMessageFromConsultant()
    }

    private fun checkInputFieldsEnabledHasNotificationMessage() {
        ChatMainScreen {
            assert("Приветственное сообщение не должно отображаться") {
                welcomeScreen.isGone()
            }
            assert("Поле ввода должно отображаться") {
                inputEditView.isVisible()
            }
            assert("В списке сообщений должно отображаться сообщение с текстом: \"$scheduleNotificationMessage\"") {
                chatItemsRecyclerView.isVisible()
                chatItemsRecyclerView.hasDescendant { containsText(scheduleNotificationMessage) }
            }
            inputEditView.typeText(textToSend)
            assert("Кнопка \"Отправить\" должны отображаться") {
                sendMessageBtn.isVisible()
                sendMessageBtn.click()
            }
            assert("В списке сообщений должно отображаться сообщение с текстом: \"$textToSend\"") {
                chatItemsRecyclerView.isVisible()
                chatItemsRecyclerView.hasDescendant { containsText(textToSend) }
            }
        }
    }

    private fun checkInputFieldsDisabledHasNotificationMessage() {
        ChatMainScreen {
            assert("Приветственное сообщение не должно отображаться") {
                welcomeScreen.isGone()
            }
            assert("Поле ввода должно отображаться") {
                inputEditView.isVisible()
            }
            assert("В списке сообщений должно отображаться сообщение с текстом: \"$scheduleNotificationMessage\"") {
                chatItemsRecyclerView.isVisible()
                chatItemsRecyclerView.hasDescendant { containsText(scheduleNotificationMessage) }
            }
            assert("Кнопка \"Отправить\" должны быть неактивной") {
                sendMessageBtn.isDisabled()
            }
        }
    }

    private fun checkInputFieldsDisabledHasNotificationMessageWithMessageFromConsultant() {
        ChatMainScreen {
            assert("Приветственное сообщение не должно отображаться") {
                welcomeScreen.isGone()
            }
            assert("Поле ввода должно отображаться") {
                inputEditView.isVisible()
            }
            assert("В списке сообщений должно отображаться сообщение с текстом: \"$scheduleNotificationMessage\"") {
                chatItemsRecyclerView.isVisible()
                chatItemsRecyclerView.hasDescendant { containsText(scheduleNotificationMessage) }
            }
            assert("Кнопка \"Отправить\" должны быть неактивной") {
                sendMessageBtn.isDisabled()
            }
            sendMessageToSocket(operatorHelloMessage)
            assert("В списке сообщений должно отображаться сообщение с текстом: \"$operatorHelloMessageText\"") {
                chatItemsRecyclerView.isVisible()
                chatItemsRecyclerView.hasDescendant { containsText(operatorHelloMessageText) }
            }
        }
    }

    private fun checkInputFieldsEnabledAndHasNotificationMessageWithMessageFromConsultant() {
        ChatMainScreen {
            assert("Приветственное сообщение не должно отображаться") {
                welcomeScreen.isGone()
            }
            assert("Поле ввода должно отображаться") {
                inputEditView.isVisible()
            }
            assert("В списке сообщений должно отображаться сообщение с текстом: \"$scheduleNotificationMessage\"") {
                chatItemsRecyclerView.isVisible()
                chatItemsRecyclerView.hasDescendant { containsText(scheduleNotificationMessage) }
            }
            inputEditView.typeText(textToSend)
            assert("Кнопка \"Отправить\" должна отображаться") {
                sendMessageBtn.isVisible()
                sendMessageBtn.click()
            }
            assert("В списке сообщений должно отображаться сообщение с текстом: \"$textToSend\"") {
                chatItemsRecyclerView.isVisible()
                chatItemsRecyclerView.hasDescendant { containsText(textToSend) }
            }
            sendMessageToSocket(operatorHelloMessage)
            assert("В списке сообщений должно отображаться сообщение с текстом: \"$operatorHelloMessageText\"") {
                chatItemsRecyclerView.isVisible()
                chatItemsRecyclerView.hasDescendant { containsText(operatorHelloMessageText) }
            }
        }
    }

    private fun checkSendMessageFromClient() {
        ChatMainScreen {
            assert("Поле ввода и приветственное сообщение должны отображаться") {
                welcomeScreen.isVisible()
                inputEditView.isVisible()
            }
            inputEditView.typeText(textToSend)
            assert("Кнопка \"Отправить\" должна отображаться") {
                sendMessageBtn.isVisible()
                sendMessageBtn.click()
            }
            assert("В списке сообщений должно отображаться сообщение с текстом: \"$textToSend\"") {
                chatItemsRecyclerView.isVisible()
                chatItemsRecyclerView.hasDescendant { containsText(textToSend) }
            }
        }
    }

    private fun checkInputFieldsNoClientInfoResponse() {
        ChatMainScreen {
            assert("Приветственное сообщение не должны отображаться") {
                welcomeScreen.isGone()
            }
            assert("Поле ввода не должно отображаться") {
                inputEditView.isNotDisplayed()
            }
            assert("Кнопка отправить не должна отображаться") {
                sendMessageBtn.isNotDisplayed()
            }
        }
    }
}
