package io.edna.threads.demo.mainChatScreen.mainTests

import androidx.test.espresso.PerformException
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import io.edna.threads.demo.BaseFilesTestCase
import io.edna.threads.demo.R
import io.edna.threads.demo.assert
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import io.edna.threads.demo.waitListForNotEmpty
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImagesTest : BaseFilesTestCase() {
    private var uiDevice: UiDevice? = null

    @Before
    fun before() {
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun sendImageTest() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()

        ChatMainScreen {
            addAttachmentBtn {
                assert("Кнопка прикрепления аттача должна быть видимой и кликабельной", ::isVisible, ::isClickable)
                click()
            }
            val isRecyclerHasItems = try {
                bottomGalleryRecycler.getSize() > 0
            } catch (exc: PerformException) {
                false
            }
            if (isRecyclerHasItems.not()) {
                val usualFileNameLength = "test_image2.jpg".length
                val fileName = copyFileToDownloads("test_files/test_image2.jpg")
                if (fileName?.length != null && fileName.length > usualFileNameLength) {
                    fileNamesToDelete.add(fileName)
                }
                Thread.sleep(500)
                pressBack()
                addAttachmentBtn { click() }
            }
            bottomGalleryRecycler {
                firstChild<ChatMainScreen.BottomGalleryItem> { click() }
            }
            sendImageBtn {
                assert("Кнопка отправки сообщения должна быть кликабельной") { isClickable() }
                click()
            }

            assert(chatItemsRecyclerView.getSize() > 0) { "Список не должен быть пустым" }

            chatItemsRecyclerView {
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    assert("Картинка должна быть кликабельной") { image.isClickable() }
                    image.click()
                }
            }
            imagePager {
                assert("Image pager (горизонтальная карусель картинок) не должен быть пустым") { isVisible() }
                assert("Должно быть открытым первое изображение (индекс 0)") { isAtPage(0) }
            }
        }
    }

    @Test
    fun imagesHistoryTest() {
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_images_response))
        openChatFromDemoLoginPage()
        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(5000)

                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    val textToCheck = "Великолепно! Как и вот это."
                    assert("Последний элемент списка должен содержать текст: \"$textToCheck\"") {
                        itemText.containsText(textToCheck)
                    }
                }

                childAt<ChatMainScreen.ChatRecyclerItem>(1) {
                    image.click()
                }
            }
            imagePager {
                assert("Image pager (горизонтальная карусель картинок) не должен быть пустым") { isVisible() }
                assert("Должно быть открытым первое изображение (индекс 0)") { isAtPage(0) }
            }
        }
    }

    @Test
    fun operatorImageQuoteTest() {
        val textToType = "Such a beautiful image!"
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_images_response))
        openChatFromDemoLoginPage()

        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(5000)
                assert("Список сообщений должен быть видим") { isVisible() }
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    assert("Последнее сообщение в списке должно быть видимым") { isVisible() }
                    perform { longClick() }
                }
            }
            replyBtn {
                assert("Кнопка повтора должна быть видимой") { isVisible() }
                click()
            }
            quoteText {
                val textToCheck = "Великолепно! Как и вот это."
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
            UiScrollable(UiSelector().scrollable(true).instance(0)).scrollToEnd(5)
            chatItemsRecyclerView {
                assert("Список элементов должен быть видимым") { isVisible() }
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    assert("Последний элемент в списке должен содержать текст: \"$textToType\"") {
                        itemText.containsText(textToType)
                    }
                }
            }
        }
    }
}
