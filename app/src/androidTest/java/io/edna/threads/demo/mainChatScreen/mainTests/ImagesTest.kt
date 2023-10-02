package io.edna.threads.demo.mainChatScreen.mainTests

import androidx.test.espresso.PerformException
import androidx.test.ext.junit.runners.AndroidJUnit4
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
                copyFileToDownloads("test_files/test_image2.jpg")
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

            chatItemsRecyclerView {
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    image.isClickable()
                    image.click()
                }
            }
            imagePager.isVisible()
            imagePager.isAtPage(0)

            pressBack()
            chatItemsRecyclerView { waitListForNotEmpty(5000) }

            sendMessageToSocket(TestMessages.operatorImageMessage)

            assert(chatItemsRecyclerView.getSize() > 1)
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
                    itemText.containsText("Великолепно! Как и вот это.")
                    image.isClickable()
                    image.click()
                }
            }
            imagePager.isVisible()
            imagePager.isAtPage(3)
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
            quoteImage { isVisible() }
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
