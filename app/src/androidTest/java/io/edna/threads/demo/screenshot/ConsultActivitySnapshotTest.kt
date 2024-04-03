package io.edna.threads.demo.screenshot

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.testify.ScreenshotRule
import dev.testify.annotation.ScreenshotInstrumentation
import edna.chatcenter.ui.core.models.ConsultInfo
import edna.chatcenter.ui.visual.activities.ConsultActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConsultActivitySnapshotTest {
    private val avatarUrl = "https://noednaimage.ru/1.jpg"
    private val status = "Active"
    private val name = "Operator Alisa"

    @get:Rule
    val rule = ScreenshotRule(ConsultActivity::class.java).apply {
        addIntentExtras {
            it.putParcelable(
                ConsultActivity.consultInfoKey,
                ConsultInfo(
                    photoUrl = avatarUrl,
                    status = status,
                    name = name
                )
            )
        }
    }

    @ScreenshotInstrumentation
    @Test
    fun testConsultActivity() {
        rule.assertSame()
    }
}
