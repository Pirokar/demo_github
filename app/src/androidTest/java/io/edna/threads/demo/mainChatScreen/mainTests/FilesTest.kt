package io.edna.threads.demo.mainChatScreen.mainTests

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaspersky.kaspresso.internal.extensions.other.createFileIfNeeded
import io.edna.threads.demo.BaseFilesTestCase
import io.edna.threads.demo.R
import io.edna.threads.demo.assert
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import io.edna.threads.demo.toFile
import io.edna.threads.demo.waitListForNotEmpty
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class FilesTest : BaseFilesTestCase() {
    @get:Rule
    val intentsTestRule = IntentsRule()

    @Test
    fun sendFile() {
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(getFileResult())
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        ChatMainScreen {
            val sizeBeforeSend = chatItemsRecyclerView.getSize()
            assert("Кнопка добавления файла (скрепка) должна быть кликабельной") { addAttachmentBtn.isClickable() }
            addAttachmentBtn.click()
            assert("Кнопка добавления файла в нижнем меню должна быть кликабельной") { fileBottomSheenBtn.isClickable() }
            fileBottomSheenBtn.click()
            assert("Кнопка отправки файла должна быть кликабельной") { sendMessageBtn.isClickable() }
            sendMessageBtn.click()
            assert(chatItemsRecyclerView.getSize() > sizeBeforeSend) { "Размер списка после отправки файла должен быть больше" }
        }
    }

    @Test
    fun sendFileWithText() {
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(getFileResult())
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        ChatMainScreen {
            inputEditView { isVisible() }
            welcomeScreen { isVisible() }

            inputEditView.typeText(helloTextToSend)
            val sizeBeforeSend = chatItemsRecyclerView.getSize()
            assert("Кнопка добавления файла (скрепка) должна быть кликабельной") { addAttachmentBtn.isClickable() }
            addAttachmentBtn.click()
            assert("Кнопка добавления файла в нижнем меню должна быть кликабельной") { fileBottomSheenBtn.isClickable() }
            fileBottomSheenBtn.click()
            assert("Кнопка отправки файла должна быть кликабельной") { sendMessageBtn.isClickable() }
            sendMessageBtn.click()
            assert(chatItemsRecyclerView.getSize() > sizeBeforeSend) { "Размер списка после отправки файла должен быть больше" }

            chatItemsRecyclerView {
                assert("Список сообщений должен содержать текст: \"${helloTextToSend}\"") {
                    hasDescendant { containsText(helloTextToSend) }
                }
            }
        }
    }

    @Test
    fun filesHistoryTest() {
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_files_response))
        openChatFromDemoLoginPage()
        ChatMainScreen {
            chatItemsRecyclerView {
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
                assert(getSize() == 7)
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

    private fun getFileResult(): Instrumentation.ActivityResult {
        val resultData = Intent()
        val file = File(context.filesDir, "test_image2.jpg")
        if (!file.exists()) {
            file.deleteOnExit()
            context.assets.open("test_files/test_image2.jpg").toFile(file.createFileIfNeeded())
        }
        resultData.data = Uri.fromFile(file)
        return Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
    }
}
