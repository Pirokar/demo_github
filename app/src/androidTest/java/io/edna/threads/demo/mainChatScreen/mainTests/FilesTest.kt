package io.edna.threads.demo.mainChatScreen.mainTests

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.edna.threads.demo.BaseFilesTestCase
import io.edna.threads.demo.R
import io.edna.threads.demo.assert
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import io.edna.threads.demo.waitListForNotEmpty
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FilesTest : BaseFilesTestCase() {
    @Test
    fun filesHistoryTest() {
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_files_response))
        openChatFromDemoLoginPage()
        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(5000)
                childAt<ChatMainScreen.ChatRecyclerItem>(5) {
                    val textToCheck = "Интересный файл"
                    assert("Элемент с индексом 5 должен содержать текст: \"$textToCheck\"") {
                        itemText.containsText(textToCheck)
                    }
                }
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    val textToCheck = "Ваш тоже"
                    assert("Последний элемент должен содержать текст: \"$textToCheck\"") {
                        itemText.containsText(textToCheck)
                    }
                }
                assert(getSize() == 8)
            }
        }
    }

    @Test
    fun operatorFileQuoteTest() {
        val textToType = "Such a beautiful file!"
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_files_response))
        openChatFromDemoLoginPage()

        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(5000)
                assert("Список элементов должен быть видимым") { isVisible() }
                isVisible()
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    assert("Последний элемент в списке должен быть видимым") { isVisible() }
                    perform { longClick() }
                }
            }
            replyBtn {
                assert("Кнопка повтора должна быть видимой") { isVisible() }
                click()
            }
            Thread.sleep(500)
            quoteText {
                val textToCheck = "Ваш тоже!"
                assert("Процитированный текст должен быть видим") { isVisible() }
                assert("Процитированный текст должен содержать строку: \"$textToCheck\"") { hasText(textToCheck) }
            }
            quoteHeader {
                val textToCheck = "Оператор Елена"
                assert("Заголовок цитаты должен быть видим") { isVisible() }
                assert("Заголовок цитаты должен содержать строку: \"$textToCheck\"") { hasText(textToCheck) }
            }
            quoteClear {
                assert("Кнопка очистка цитирования должна быть видимой") { isVisible() }
            }

            inputEditView {
                assert("Поле для ввода должно быть видимым") { isVisible() }
                typeText(textToType)
            }
            sendMessageBtn {
                assert("Кнопка отправки сообщения должна быть видимой") { isVisible() }
                click()
            }
            closeSoftKeyboard()
            chatItemsRecyclerView {
                assert("Список элементов должен быть видимым") { isVisible() }
                scrollTo(0)
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    assert("Последний элемент в списке должен содержать текст: \"$textToType\"") {
                        itemText.containsText(textToType)
                    }
                }
            }
        }
    }
}
