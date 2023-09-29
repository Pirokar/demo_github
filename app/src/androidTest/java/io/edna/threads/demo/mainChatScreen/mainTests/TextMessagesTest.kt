package io.edna.threads.demo.mainChatScreen.mainTests

import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.edna.threads.demo.BaseTestCase
import io.edna.threads.demo.R
import io.edna.threads.demo.TestMessages
import io.edna.threads.demo.appCode.activity.MainActivity
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
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

    private val textToSend = "Hello, Edna! This is a test message"

    init {
        applyDefaultUserToDemoApp()
        prepareWsMocks()
    }

    @Test
    fun textMessages() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()

        ChatMainScreen {
            inputEditView { isVisible() }
            welcomeScreen { isVisible() }

            inputEditView.typeText(textToSend)
            sendMessageBtn {
                isVisible()
                click()
            }
            chatItemsRecyclerView {
                isVisible()
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    itemText.containsText(textToSend)
                }
            }
        }

        sendMessageFromOperator()

        ChatMainScreen {
            chatItemsRecyclerView {
                isVisible()
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    itemText.containsText("привет!")
                }
            }
        }
    }

    @Test
    fun historyTest() {
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_text_response))
        openChatFromDemoLoginPage()
        ChatMainScreen {
            inputEditView { isVisible() }
            chatItemsRecyclerView {
                isVisible()
                childAt<ChatMainScreen.ChatRecyclerItem>(1) {
                    itemText.containsText("Добрый день! Мы создаем экосистему бизнеса")
                }

                hasDescendant { containsText("Добро пожаловать в наш чат") }
                hasDescendant { containsText("Edna – современное решение для построения диалога с клиентом") }

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
    fun operatorQuoteTest() {
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

        val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
        if (clipboard?.hasPrimaryClip() == true) {
            assert(clipboard.primaryClip?.getItemAt(0)?.text == "привет!")
        } else {
            assert(false)
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
}
