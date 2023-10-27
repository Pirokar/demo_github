package io.edna.threads.demo.mainChatScreen.mainTests

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import io.edna.threads.demo.BaseFilesTestCase
import io.edna.threads.demo.R
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
            UiScrollable(UiSelector().scrollable(true).instance(0)).scrollToEnd(5)
            chatItemsRecyclerView {
                isVisible()
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    itemText.containsText(textToType)
                }
            }
        }
    }
}
