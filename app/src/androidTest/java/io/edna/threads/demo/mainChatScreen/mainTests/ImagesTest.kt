package io.edna.threads.demo.mainChatScreen.mainTests

import androidx.test.espresso.PerformException
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import io.edna.threads.demo.BaseFilesTestCase
import io.edna.threads.demo.R
import io.edna.threads.demo.TestMessages
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import io.edna.threads.demo.waitListForNotEmpty
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImagesTest : BaseFilesTestCase() {
    @Test
    fun sendReceiveImage() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()

        ChatMainScreen {
            addAttachmentBtn {
                isVisible()
                isClickable()
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
                isClickable()
                click()
            }

            assert(chatItemsRecyclerView.getSize() > 0)

            sendMessageToSocket(TestMessages.operatorImageMessage)

            assert(chatItemsRecyclerView.getSize() > 1)

            chatItemsRecyclerView {
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    image.isClickable()
                    image.click()
                }
            }
            imagePager.isVisible()
            imagePager.isAtPage(1)
        }
    }

    @Test
    fun imagesHistoryTest() {
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_images_response))
        openChatFromDemoLoginPage()
        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(5000)

                UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                UiScrollable(UiSelector().scrollable(true).instance(0)).scrollIntoView(
                    UiSelector().textContains("10:05").instance(0)
                )

                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    itemText.containsText("Великолепно! Как и вот это.")
                }

                childAt<ChatMainScreen.ChatRecyclerItem>(1) {
                    image.isClickable()
                    image.click()
                }
            }
            imagePager.isVisible()
            imagePager.isAtPage(0)
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
                hasText("Великолепно! Как и вот это.")
            }
            quoteHeader {
                isVisible()
                hasText("Оператор Елена")
            }
            quoteClear { isVisible() }

            inputEditView {
                isVisible()
                typeText(textToType)
            }
            sendMessageBtn {
                isVisible()
                click()
            }
            closeSoftKeyboard()
            chatItemsRecyclerView {
                isVisible()
                scrollTo(0)
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    itemText.containsText(textToType)
                }
            }
        }
    }
}
