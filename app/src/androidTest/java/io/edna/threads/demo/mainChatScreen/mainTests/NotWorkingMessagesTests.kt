package io.edna.threads.demo.mainChatScreen.mainTests

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import im.threads.business.transport.threadsGate.ThreadsGateTransport
import io.edna.threads.demo.BaseTestCase
import io.edna.threads.demo.BuildConfig
import io.edna.threads.demo.TestMessages
import io.edna.threads.demo.TestMessages.operatorHelloMessage
import io.edna.threads.demo.appCode.activity.MainActivity
import io.edna.threads.demo.assert
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.anyOrNull

@RunWith(AndroidJUnit4::class)
class NotWorkingMessagesTests : BaseTestCase() {

    private val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
    private val coroutineScope = CoroutineScope(Dispatchers.Unconfined)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<MainActivity>(intent)

    private val textToSend = "Hello, Edna! This is test message for test schedule."
    private val scheduleNotificationMessage = "Война войной, а обед по расписанию."
    private val operatorHelloMessageText = "привет!"

    private val onlySettingsAnswer = listOf(
        TestMessages.attachmentSettingsWsMessage
    )

    private val clientInfoWsNoWorkingDaysScheduleMessagesSendDuringInactive = listOf(
        TestMessages.scheduleNoWorkingDaysSendDuringInactiveWsMessage,
        TestMessages.attachmentSettingsWsMessage
    )

    private val clientInfoWsNoWorkingDaysScheduleMessagesNotSendDuringInactive = listOf(
        TestMessages.scheduleNoWorkingDaysNotSendDuringInactiveWsMessage,
        TestMessages.attachmentSettingsWsMessage
    )

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
        prepareWsMocks(onlySettingsAnswer)
        openChatFromDemoLoginPage()
        checkSendMessageFromClient()
    }

    @Test
    fun sendMessageWhenScheduleActive() {
        prepareWsMocks()
        openChatFromDemoLoginPage()
        checkSendMessageFromClient()
    }

    @Test
    fun sendMessageWhenScheduleInActiveSendDuringInactive() {
        prepareWsMocks(clientInfoWsNoWorkingDaysScheduleMessagesSendDuringInactive)
        openChatFromDemoLoginPage()
        checkInputFieldsEnabledHasNotificationMessage()
    }

    @Test
    fun sendMessageWhenScheduleInActiveSendDuringInactiveWithMessageFromConsultant() {
        prepareWsMocks(clientInfoWsNoWorkingDaysScheduleMessagesSendDuringInactive)
        openChatFromDemoLoginPage()
        checkInputFieldsEnabledHasNotificationMessageWithMessageFromConsultant()
    }

    @Test
    fun sendMessageWhenScheduleInActiveNotSendDuringInactive() {
        prepareWsMocks(clientInfoWsNoWorkingDaysScheduleMessagesNotSendDuringInactive)
        openChatFromDemoLoginPage()
        checkInputFieldsDisabledHasNotificationMessage()
    }

    @Test
    fun sendMessageWhenScheduleInActiveNotSendDuringInactiveWithMessageFromConsultant() {
        prepareWsMocks(clientInfoWsNoWorkingDaysScheduleMessagesNotSendDuringInactive)
        openChatFromDemoLoginPage()
        checkInputFieldsDisabledHasNotificationMessageWithMessageFromConsultant()
    }

    private fun prepareWsMocks(answers: List<String>) {
        BuildConfig.IS_MOCK_WEB_SERVER.set(true)
        MockitoAnnotations.openMocks(this)
        Mockito.`when`(okHttpClient.newWebSocket(anyOrNull(), anyOrNull())).thenReturn(socket)
        Mockito.doAnswer { mock: InvocationOnMock ->
            val stringArg = mock.arguments[0] as String
            val (answer, isClientInfo) = getAnswersForWebSocket(stringArg)
            answer?.let { wsAnswer ->
                sendMessageToSocket(wsAnswer)
                if (isClientInfo) {
                    answers.forEach { sendMessageToSocket(it) }
                }
            }
            null
        }.`when`(socket).send(Mockito.anyString())

        coroutineScope.launch {
            ThreadsGateTransport.transportUpdatedChannel.collect {
                it.client = okHttpClient
                it.webSocket = null
            }
        }
    }

    private fun checkInputFieldsEnabledHasNotificationMessage() {
        ChatMainScreen {
            assert("Приветственное сообщение не должно отображаться") {
                welcomeScreen.isNotDisplayed()
            }
            assert("Поле ввода должно отображаться") {
                inputEditView.isVisible()
            }
            assert("В списке сообщений должно отображатся сообщение с текстом: \"$scheduleNotificationMessage\"") {
                chatItemsRecyclerView.isVisible()
                chatItemsRecyclerView.hasDescendant { containsText(scheduleNotificationMessage) }
            }
            inputEditView.typeText(textToSend)
            assert("Кнопка \"Отправить\" должны отображаться") {
                sendMessageBtn.isVisible()
                sendMessageBtn.click()
            }
            assert("В списке сообщений должно отображатся сообщение с текстом: \"$textToSend\"") {
                chatItemsRecyclerView.isVisible()
                chatItemsRecyclerView.hasDescendant { containsText(textToSend) }
            }
        }
    }

    private fun checkInputFieldsDisabledHasNotificationMessage() {
        ChatMainScreen {
            assert("Приветственное сообщение не должно отображаться") {
                welcomeScreen.isNotDisplayed()
            }
            assert("Поле ввода должно отображаться") {
                inputEditView.isVisible()
            }
            assert("В списке сообщений должно отображатся сообщение с текстом: \"$scheduleNotificationMessage\"") {
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
                welcomeScreen.isNotDisplayed()
            }
            assert("Поле ввода должно отображаться") {
                inputEditView.isVisible()
            }
            assert("В списке сообщений должно отображатся сообщение с текстом: \"$scheduleNotificationMessage\"") {
                chatItemsRecyclerView.isVisible()
                chatItemsRecyclerView.hasDescendant { containsText(scheduleNotificationMessage) }
            }
            assert("Кнопка \"Отправить\" должны быть неактивной") {
                sendMessageBtn.isDisabled()
            }
            sendMessageToSocket(operatorHelloMessage)
            assert("В списке сообщений должно отображатся сообщение с текстом: \"$operatorHelloMessageText\"") {
                chatItemsRecyclerView.isVisible()
                chatItemsRecyclerView.hasDescendant { containsText(operatorHelloMessageText) }
            }
        }
    }

    private fun checkInputFieldsEnabledHasNotificationMessageWithMessageFromConsultant() {
        ChatMainScreen {
            assert("Приветственное сообщение не должно отображаться") {
                welcomeScreen.isNotDisplayed()
            }
            assert("Поле ввода должно отображаться") {
                inputEditView.isVisible()
            }
            assert("В списке сообщений должно отображатся сообщение с текстом: \"$scheduleNotificationMessage\"") {
                chatItemsRecyclerView.isVisible()
                chatItemsRecyclerView.hasDescendant { containsText(scheduleNotificationMessage) }
            }
            inputEditView.typeText(textToSend)
            assert("Кнопка \"Отправить\" должна отображаться") {
                sendMessageBtn.isVisible()
                sendMessageBtn.click()
            }
            assert("В списке сообщений должно отображатся сообщение с текстом: \"$textToSend\"") {
                chatItemsRecyclerView.isVisible()
                chatItemsRecyclerView.hasDescendant { containsText(textToSend) }
            }
            sendMessageToSocket(operatorHelloMessage)
            assert("В списке сообщений должно отображатся сообщение с текстом: \"$operatorHelloMessageText\"") {
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
            assert("В списке сообщений должно отображатся сообщение с текстом: \"$textToSend\"") {
                chatItemsRecyclerView.isVisible()
                chatItemsRecyclerView.hasDescendant { containsText(textToSend) }
            }
        }
    }

    private fun checkInputFieldsNoClientInfoResponse() {
        ChatMainScreen {
            assert("Приветственное сообщение не должны отображаться") {
                welcomeScreen.isNotDisplayed()
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
