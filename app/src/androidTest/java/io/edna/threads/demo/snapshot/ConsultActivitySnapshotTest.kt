package io.edna.threads.demo.snapshot

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.testify.ScreenshotRule
import dev.testify.annotation.ScreenshotInstrumentation
import im.threads.business.models.ConsultInfo
import im.threads.ui.activities.ConsultActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConsultActivitySnapshotTest {
    private val avatarUrl = "https://img.freepik.com/free-photo/pretty-smiling-joyfully-female-with-fair-hair-dressed-casually-looking-with-satisfaction_176420-15187.jpg?w=826&t=st=1660821591~exp=1660822191~hmac=03ad70b2616db9df41c6174265f7a97806338958ef738236badc63c5b0ff1897"
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
