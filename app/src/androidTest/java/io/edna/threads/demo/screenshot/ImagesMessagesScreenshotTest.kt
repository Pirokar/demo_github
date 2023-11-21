package io.edna.threads.demo.screenshot

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.testify.annotation.ScreenshotInstrumentation
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImagesMessagesScreenshotTest : BaseScreenshotTestCase() {
    @ScreenshotInstrumentation
    @Test
    fun imagesMessagesScreenshotTextAtEnd() {
        openDemoExample(stringsProvider.images)
        ChatMainScreen {
            chatItemsRecyclerView { scrollToEnd() }
        }
        Thread.sleep(2000)
        screenshotRule.assertSame()
    }

    @ScreenshotInstrumentation
    @Test
    fun imagesMessagesScreenshotTextAtStart() {
        openDemoExample(stringsProvider.images)
        ChatMainScreen {
            chatItemsRecyclerView { scrollToStart() }
        }
        Thread.sleep(2000)
        screenshotRule.assertSame()
    }

    @ScreenshotInstrumentation
    @Test
    fun imagesMessagesScreenshotTextAtTheMiddle() {
        openDemoExample(stringsProvider.images)
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
