package io.edna.threads.demo.mainChatScreen.mainTests

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.edna.threads.demo.BaseFilesTestCase
import io.edna.threads.demo.R
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
                    itemText.containsText("Интересный файл")
                }
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    itemText.containsText("Ваш тоже")
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
                hasText("Ваш тоже!")
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
