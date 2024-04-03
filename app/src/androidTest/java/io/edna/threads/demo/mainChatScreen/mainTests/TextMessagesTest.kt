package io.edna.threads.demo.mainChatScreen.mainTests

import android.app.Activity
import android.app.Instrumentation
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.edna.threads.demo.BaseTestCase
import io.edna.threads.demo.R
import io.edna.threads.demo.TestMessages
import io.edna.threads.demo.appCode.activity.MainActivity
import io.edna.threads.demo.assert
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import io.edna.threads.demo.waitListForNotEmpty
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Date

@RunWith(AndroidJUnit4::class)
class TextMessagesTest : BaseTestCase() {
    private val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<MainActivity>(intent)

    @get:Rule
    val intentsTestRule = IntentsRule()

    init {
        applyDefaultUserToDemoApp()
        prepareWsMocks()
    }

    @Before
    fun stubAllExternalIntents() {
        intending(not(isInternal())).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK,
                null
            )
        )
    }

    @Test
    fun textMessages() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()

        sendHelloMessageFromUser()
        ChatMainScreen.chatItemsRecyclerView {
            assert("Список сообщений должен быть видим") { isVisible() }
            assert("Список сообщений должен содержать текст: \"${helloTextToSend}\"") {
                hasDescendant { containsText(helloTextToSend) }
            }
        }

        sendMessageFromOperator()
        ChatMainScreen {
            chatItemsRecyclerView {
                assert("Список сообщений должен быть видим") { isVisible() }
                val textToCheck = "привет!"
                assert("Список сообщений должен содержать текст: \"${helloTextToSend}\"") {
                    hasDescendant { containsText(textToCheck) }
                }
            }
        }
    }

    @Test
    fun historyTest() {
        openMessagesHistory()
        ChatMainScreen {
            chatItemsRecyclerView {
                childAt<ChatMainScreen.ChatRecyclerItem>(1) {
                    val textToCheck = "Добрый день! Мы создаем экосистему бизнеса"
                    assert("Сообщение с индексом 1 должно содержать текст: \"$textToCheck\"") {
                        itemText.containsText(textToCheck)
                    }
                }

                val welcomeText = "Добро пожаловать в наш чат"
                assert("Список должен содержать сообщение: \"$welcomeText\"") {
                    hasDescendant { containsText(welcomeText) }
                }

                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    val textToCheck = "Тогда до связи!"
                    assert("Последний элемент списка должен содержать сообщение: \"$textToCheck\"") {
                        itemText.containsText(textToCheck)
                    }
                }
            }
            chatItemsRecyclerView {
                assert("Список должен содержать сообщение: \"Отлично! Давайте проверим ваши контакты.\"") {
                    hasDescendant { containsText("Отлично! Давайте проверим ваши контакты. Ваш email: info@edna.ru, телефон: +7 (495) 609-60-80. Верно?") }
                }
                assert("Список должен содержать сообщение: \"Именно! А еще у нас есть различные...\"") {
                    hasDescendant { containsText("Именно! А еще у нас есть различные каналы коммуникации с клиентами! Подробнее: https://edna.ru/channels/") }
                }
                assert("Список должен содержать сообщение: \"Да, все верно\"") {
                    hasDescendant { containsText("Да, все верно") }
                }
            }
        }
    }

    @Test
    fun operatorTextQuoteTest() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        sendMessageFromOperator()

        ChatMainScreen {
            chatItemsRecyclerView {
                assert("Список сообщений должен быть видим") { isVisible() }
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    assert("Последнее сообщение должно быть видимо") { isVisible() }
                    perform { longClick() }
                }
            }
            replyBtn {
                assert("Кнопка повтора должна быть видимой") { isVisible() }
                click()
            }
            quoteText {
                val textToCheck = "привет!"
                assert("Процитированный текст должен быть видим") { isVisible() }
                assert("Процитированный текст должен содержать строку: \"$textToCheck\"") { hasText(textToCheck) }
            }
            quoteHeader {
                assert("Заголовок цитаты должен быть видим") { isVisible() }
                assert("Заголовок цитаты не должен быть пустым") { hasAnyText() }
            }
            quoteClear {
                assert("Кнопка очистка цитирования должна быть видимой") { isVisible() }
            }
            inputEditView {
                assert("Поле для ввода должно быть видимым") { isVisible() }
                typeText(helloTextToSend)
            }
            sendMessageBtn {
                assert("Кнопка отправки сообщения должна быть видимой") { isVisible() }
                click()
            }
            chatItemsRecyclerView {
                assert("Список должен содержать сообщение: \"$helloTextToSend\"") {
                    hasDescendant { containsText(helloTextToSend) }
                }
            }
        }
    }

    @Test
    fun copyOperatorMessageTest() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        sendMessageFromOperator()

        ChatMainScreen {
            chatItemsRecyclerView {
                assert("Список сообщений должен быть видим") { isVisible() }
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    assert("Последний элемент списка должен быть видим") { isVisible() }
                    perform { longClick() }
                }
            }
            copyBtn {
                assert("Кнопка копирования должна быть видимой и кликабельной", ::isVisible, ::isClickable)
                click()
            }
        }

        Handler(Looper.getMainLooper()).post {
            val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
            val textToCheck = "привет!"
            if (clipboard?.hasPrimaryClip() == true) {
                assert(clipboard.primaryClip?.getItemAt(0)?.text == textToCheck) {
                    "Буфер обмена должен содержать текст: \"$textToCheck\""
                }
            } else {
                assert(false) {
                    "Буфер обмена должен содержать текст: \"$textToCheck\""
                }
            }
        }
    }

    @Test
    fun testIsEmailClickable() {
        openMessagesHistory()

        ChatMainScreen {
            chatItemsRecyclerView {
                childAt<ChatMainScreen.ChatRecyclerItem>(11) {
                    itemText.clickSpanWithText("info@edna.ru")
                }
            }
        }
        intended(
            allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData("mailto:info@edna.ru")
            )
        )
    }

    @Test
    fun testIsPhoneClickable() {
        openMessagesHistory()
        clickForTelSpanAndCheck(11, "+7 (495) 609-60-80")

        sendCustomMessageFromUser("9261363821")
        clickForTelSpanAndCheck(textToClick = "9261363821")

        sendCustomMessageFromUser("+79855687102")
        clickForTelSpanAndCheck(textToClick = "+79855687102")

        sendCustomMessageFromUser("+7 977 409 27 19")
        clickForTelSpanAndCheck(textToClick = "+7 977 409 27 19")

        sendCustomMessageFromUser("+7-843-552-83-17")
        clickForTelSpanAndCheck(textToClick = "+7-843-552-83-17")

        sendCustomMessageFromUser("+7-811-687-0002")
        clickForTelSpanAndCheck(textToClick = "+7-811-687-0002")

        sendCustomMessageFromUser("+7 971 971-48-21")
        clickForTelSpanAndCheck(textToClick = "+7 971 971-48-21")

        sendCustomMessageFromUser("+7 (999) 999-99-99")
        clickForTelSpanAndCheck(textToClick = "+7 (999) 999-99-99")

        sendCustomMessageFromUser("+7 (421) 123.45.65")
        clickForTelSpanAndCheck(textToClick = "+7 (421) 123.45.65")

        sendCustomMessageFromUser("+7.927.555.55.55")
        clickForTelSpanAndCheck(textToClick = "+7.927.555.55.55")
    }

    @Test
    fun testIsUrlClickable() {
        openMessagesHistory()
        val idx1TextToClick = "https://edna.ru/"

        ChatMainScreen {
            chatItemsRecyclerView {
                childAt<ChatMainScreen.ChatRecyclerItem>(1) {
                    itemText.clickSpanWithText(idx1TextToClick)
                }
            }
        }
        assert("Сообщение с индексом 1 должно быть кликабельно и содержать текст: \"$idx1TextToClick\"") {
            intended(
                allOf(
                    hasAction(Intent.ACTION_VIEW),
                    hasData(idx1TextToClick)
                )
            )
        }

        val idx11TextToClick = "https://edna.ru/channels/"
        ChatMainScreen {
            chatItemsRecyclerView {
                childAt<ChatMainScreen.ChatRecyclerItem>(10) {
                    itemText.clickSpanWithText(idx11TextToClick)
                }
            }
        }
        assert("Сообщение с индексом 1 должно быть кликабельно и содержать текст: \"$idx11TextToClick\"") {
            intended(
                allOf(
                    hasAction(Intent.ACTION_VIEW),
                    hasData(idx11TextToClick)
                )
            )
        }
    }

    @Test
    fun testThreadIsClosed() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        sendHelloMessageFromUser()
        sendMessageToSocket(TestMessages.threadIsClosed)

        ChatMainScreen.chatItemsRecyclerView {
            val textToCheck = "Диалог завершен. Будем рады проконсультировать вас снова!"
            assert("Список сообщений должен быть видим") { isVisible() }
            assert("Список должен содержать сообщение: \"$textToCheck\"") {
                hasDescendant { containsText(textToCheck) }
            }
        }
    }

    @Test
    fun testClientIsBlocked() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        sendHelloMessageFromUser()
        sendMessageToSocket(TestMessages.clientIsBlocked)

        ChatMainScreen.chatItemsRecyclerView {
            val textToCheck = "Вы заблокированы, дальнейшее общение с оператором ограничено"
            assert("Список сообщений должен быть видим") { isVisible() }
            assert("Список должен содержать сообщение: \"$textToCheck\"") {
                hasDescendant { containsText(textToCheck) }
            }
        }
    }

    @Test
    fun testClientTransfer() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        sendHelloMessageFromUser()
        sendMessageToSocket(TestMessages.operatorTransfer)
        sendMessageToSocket(TestMessages.operatorAssigned)

        ChatMainScreen {
            chatItemsRecyclerView {
                assert("Список сообщений должен быть видим") { isVisible() }
                assert("Список должен содержать сообщение: \"Для решения вопроса диалог переводится другому оператору\"") {
                    hasDescendant { containsText("Для решения вопроса диалог переводится другому оператору") }
                }
                assert("Список должен содержать сообщение: \"Вам ответит Оператор0 Иванович\"") {
                    hasDescendant { containsText("Вам ответит Оператор0 Иванович") }
                }
            }
            assert("Имя оператора в тулбаре должно быть: \"Оператор0 Иванович\"") {
                toolbarOperatorName.hasText("Оператор0 Иванович")
            }
        }
    }

    @Test
    fun testOperatorWaiting() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        sendHelloMessageFromUser()
        sendMessageToSocket(TestMessages.operatorWaiting)

        ChatMainScreen {
            val operatorName = context.getString(edna.chatcenter.R.string.ecc_searching_operator)
            assert("Имя оператора в тулбаре должно быть: \"$operatorName\"") {
                toolbarOperatorName.hasText(operatorName)
            }
            val textToCheck = "Среднее время ожидания ответа составляет 2 минуты"
            chatItemsRecyclerView {
                assert("Список сообщений должен содержать текст: \"$textToCheck\"") {
                    hasDescendant { containsText(textToCheck) }
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

    private fun openMessagesHistory() {
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_text_response))
        openChatFromDemoLoginPage()
        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(5000)
                assert("Список сообщений должен быть видим") { isVisible() }
                assert(getSize() == 14) { "Список содержит неверное количество сообщений" }
            }
        }
    }

    private fun sendMessageFromOperator() {
        val currentTimeMs = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val timeFormat = SimpleDateFormat("HH:mm:ss")
        val currentDateObj = Date(currentTimeMs)
        val currentDate = dateFormat.format(currentDateObj)
        val currentTime = timeFormat.format(currentDateObj)

        val operatorMessage = TestMessages.operatorHelloMessage
            .replace("2023-09-25", currentDate)
            .replace("13:07:29", currentTime)

        sendMessageToSocket(operatorMessage)
    }

    private fun clickForTelSpanAndCheck(positionInList: Int? = null, textToClick: String) {
        ChatMainScreen {
            chatItemsRecyclerView {
                positionInList?.let {
                    childAt<ChatMainScreen.ChatRecyclerItem>(positionInList) {
                        itemText.clickSpanWithText(textToClick)
                    }
                } ?: run {
                    lastChild<ChatMainScreen.ChatRecyclerItem>() {
                        itemText.clickSpanWithText(textToClick)
                    }
                }
            }
        }
        val phoneToCheck = textToClick
            .replace(" ", "")
            .replace("(", "")
            .replace(")", "")
            .replace("-", "")
            .replace(".", "")

        assert("Ссылка должна содержать текст: \"$phoneToCheck\" и быть кликабельной") {
            intended(
                allOf(
                    hasAction(Intent.ACTION_VIEW),
                    hasData("tel:$phoneToCheck")
                )
            )
        }
    }
}
