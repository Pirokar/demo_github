package io.edna.threads.demo.snapshot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.testify.annotation.ScreenshotInstrumentation
import io.edna.threads.demo.R
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AttachmentsSnapshotTest : SnapshotBaseTest(R.raw.snapshot_test_history_text_response_1) {
    @ScreenshotInstrumentation
    @Test
    override fun testChat() {
        rule.setEspressoActions {
            onView(withTagValue(`is`("add_attachment"))).perform(click())
        }.assertSame()
    }
}
