package edna.chatcenter.demo.screenshot

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.testify.annotation.ScreenshotInstrumentation
import edna.chatcenter.demo.kaspressoSreens.ChatMainScreen
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextMessagesScreenshotTest : BaseScreenshotTestCase() {

    @ScreenshotInstrumentation
    @Test
    fun textMessagesScreenshotTextAtEnd() {
        openDemoExample(stringsProvider.textMessages)
        ChatMainScreen {
            chatItemsRecyclerView { scrollToEnd() }
        }
        Thread.sleep(2000)
        screenshotRule.assertSame()
    }

    @ScreenshotInstrumentation
    @Test
    fun textMessagesScreenshotTextAtStart() {
        openDemoExample(stringsProvider.textMessages)
        ChatMainScreen {
            chatItemsRecyclerView { scrollToStart() }
        }
        Thread.sleep(2000)
        screenshotRule.assertSame()
    }

    @ScreenshotInstrumentation
    @Test
    fun textMessagesScreenshotTextAtTheMiddle() {
        openDemoExample(stringsProvider.textMessages)
        ChatMainScreen {
            chatItemsRecyclerView {
                Thread.sleep(500)
                val center = getSize() / 2
                scrollTo(center)
            }
        }
        Thread.sleep(2000)
        screenshotRule.assertSame()
    }
}
