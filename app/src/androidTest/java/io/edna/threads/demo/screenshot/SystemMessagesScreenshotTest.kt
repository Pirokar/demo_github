package io.edna.threads.demo.screenshot

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.testify.annotation.ScreenshotInstrumentation
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SystemMessagesScreenshotTest : BaseScreenshotTestCase() {
    @ScreenshotInstrumentation
    @Test
    fun systemMessagesScreenshotTextAtEnd() {
        openDemoExample(stringsProvider.systemMessages)
        ChatMainScreen {
            chatItemsRecyclerView { scrollToEnd() }
        }
        Thread.sleep(2000)
        screenshotRule.assertSame()
    }

    @ScreenshotInstrumentation
    @Test
    fun systemMessagesScreenshotTextAtStart() {
        openDemoExample(stringsProvider.systemMessages)
        ChatMainScreen {
            chatItemsRecyclerView { scrollToStart() }
        }
        Thread.sleep(2000)
        screenshotRule.assertSame()
    }
}
