package io.edna.threads.demo.mainChatScreen.mainTests

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.edna.threads.demo.BaseTestCase
import io.edna.threads.demo.TestMessages.defaultConfigNoScheduleMock
import io.edna.threads.demo.TestMessages.defaultConfigScheduleInactiveCanSendMock
import io.edna.threads.demo.TestMessages.defaultConfigScheduleInactiveCannotSendMock
import io.edna.threads.demo.TestMessages.operatorHelloMessage
import io.edna.threads.demo.appCode.activity.MainActivity
import io.edna.threads.demo.assert
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
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
        prepareHttpMocks(configAnswer = defaultConfigScheduleInactiveCanSendMock)
        prepareWsMocks()
        openChatFromDemoLoginPage()
        checkInputFieldsEnabledHasNotificationMessage()
    }

    @Test
    fun sendMessageWhenScheduleInActiveSendDuringInactiveWithMessageFromConsultant() {
        prepareHttpMocks(configAnswer = defaultConfigScheduleInactiveCanSendMock)
        prepareWsMocks()
        openChatFromDemoLoginPage()
        checkInputFieldsEnabledHasNotificationMessageWithMessageFromConsultant()
    }

    @Test
    fun sendMessageWhenScheduleInActiveNotSendDuringInactive() {
        prepareHttpMocks(configAnswer = defaultConfigScheduleInactiveCannotSendMock)
        prepareWsMocks()
        openChatFromDemoLoginPage()
        checkInputFieldsDisabledHasNotificationMessage()
    }

    @Test
    fun sendMessageWhenScheduleInActiveNotSendDuringInactiveWithMessageFromConsultant() {
        prepareHttpMocks(configAnswer = defaultConfigScheduleInactiveCannotSendMock)
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

    private fun checkInputFieldsEnabledHasNotificationMessageWithMessageFromConsultant() {
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
