package io.edna.threads.demo.screenshot

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.testify.annotation.ScreenshotInstrumentation
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FilesMessagesScreenshotTest : BaseScreenshotTestCase() {
    @ScreenshotInstrumentation
    @Test
    fun filesMessagesScreenshotTextAtEnd() {
        openDemoExample(stringsProvider.files)
        ChatMainScreen {
            chatItemsRecyclerView { scrollToEnd() }
        }
        Thread.sleep(2000)
        screenshotRule.assertSame()
    }

    @ScreenshotInstrumentation
    @Test
    fun filesMessagesScreenshotTextAtStart() {
        openDemoExample(stringsProvider.files)
        ChatMainScreen {
            chatItemsRecyclerView { scrollToStart() }
        }
        Thread.sleep(2000)
        screenshotRule.assertSame()
    }
}