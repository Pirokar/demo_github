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
            isVisible()
            hasDescendant { containsText(helloTextToSend) }
        }

        sendMessageFromOperator()
        ChatMainScreen {
            chatItemsRecyclerView {
                isVisible()
                hasDescendant { containsText("привет!") }
            }
        }
    }

    @Test
    fun historyTest() {
        openMessagesHistory()
        ChatMainScreen {
            chatItemsRecyclerView {
                childAt<ChatMainScreen.ChatRecyclerItem>(1) {
                    itemText.containsText("Добрый день! Мы создаем экосистему бизнеса")
                }

                hasDescendant { containsText("Добро пожаловать в наш чат") }

                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    itemText.containsText("Тогда до связи!")
                }
            }
            chatItemsRecyclerView {
                hasDescendant { containsText("То есть это все про вас?") }
                hasDescendant { containsText("Именно! А еще у нас есть различные каналы коммуникации с клиентами! Подробнее: https://edna.ru/channels/") }
                hasDescendant { containsText("Отлично! Давайте проверим ваши контакты. Ваш email: info@edna.ru, телефон: +7 (495) 609-60-80. Верно?") }
                hasDescendant { containsText("Да, все верно") }
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
                isVisible()
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    isVisible()
                    perform { longClick() }
                }
            }
            replyBtn {
                isVisible()
                click()
            }
            quoteText {
                isVisible()
                hasText("привет!")
            }
            quoteHeader {
                isVisible()
                hasAnyText()
            }
            quoteClear { isVisible() }
            inputEditView {
                isVisible()
                typeText(helloTextToSend)
            }
            sendMessageBtn {
                isVisible()
                click()
            }
            chatItemsRecyclerView {
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    itemText.containsText(helloTextToSend)
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
                isVisible()
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    isVisible()
                    perform { longClick() }
                }
            }
            copyBtn {
                isVisible()
                click()
            }
        }

        Handler(Looper.getMainLooper()).post {
            val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
            if (clipboard?.hasPrimaryClip() == true) {
                assert(clipboard.primaryClip?.getItemAt(0)?.text == "привет!")
            } else {
                assert(false)
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

        ChatMainScreen {
            chatItemsRecyclerView {
                childAt<ChatMainScreen.ChatRecyclerItem>(1) {
                    itemText.clickSpanWithText("https://edna.ru/")
                }
            }
        }
        intended(
            allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData("https://edna.ru/")
            )
        )
        ChatMainScreen {
            chatItemsRecyclerView {
                childAt<ChatMainScreen.ChatRecyclerItem>(10) {
                    itemText.clickSpanWithText("https://edna.ru/channels/")
                }
            }
        }
        intended(
            allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData("https://edna.ru/channels/")
            )
        )
    }

    @Test
    fun testThreadIsClosed() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        sendHelloMessageFromUser()
        sendMessageToSocket(TestMessages.threadIsClosed)

        ChatMainScreen.chatItemsRecyclerView {
            isVisible()
            hasDescendant { containsText("Диалог завершен. Будем рады проконсультировать вас снова!") }
        }
    }

    @Test
    fun testClientIsBlocked() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        sendHelloMessageFromUser()
        sendMessageToSocket(TestMessages.clientIsBlocked)

        ChatMainScreen.chatItemsRecyclerView {
            isVisible()
            hasDescendant { containsText("Вы заблокированы, дальнейшее общение с оператором ограничено") }
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
                isVisible()
                hasDescendant { containsText("Для решения вопроса диалог переводится другому оператору") }
                hasDescendant { containsText("Вам ответит Оператор0 Иванович") }
            }
            toolbarOperatorName.hasText("Оператор0 Иванович")
        }
    }

    @Test
    fun testOperatorWaiting() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        sendHelloMessageFromUser()
        sendMessageToSocket(TestMessages.operatorWaiting)

        ChatMainScreen {
            toolbarOperatorName.hasText(context.getString(im.threads.R.string.ecc_searching_operator))
            chatItemsRecyclerView {
                hasDescendant { containsText("Среднее время ожидания ответа составляет 2 минуты") }
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
                isVisible()

                assert(getSize() == 14)
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
        intended(
            allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData("tel:$phoneToCheck")
            )
        )
    }
}
