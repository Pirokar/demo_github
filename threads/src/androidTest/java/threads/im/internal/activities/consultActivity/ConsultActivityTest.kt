package threads.im.internal.activities.consultActivity

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import im.threads.internal.activities.ConsultActivity
import org.junit.Rule
import org.junit.Test

class ConsultActivityTest : TestCase() {
    private val avatarUrl = "https://img.freepik.com/free-photo/pretty-smiling-joyfully-female-with-fair-hair-dressed-casually-looking-with-satisfaction_176420-15187.jpg?w=826&t=st=1660821591~exp=1660822191~hmac=03ad70b2616db9df41c6174265f7a97806338958ef738236badc63c5b0ff1897"
    private val status = "Active"
    private val name = "Operator Alisa"

    private val intent = Intent(ApplicationProvider.getApplicationContext(), ConsultActivity::class.java).apply {
        putExtra(ConsultActivity.imageUrlKey, avatarUrl)
        putExtra(ConsultActivity.statusKey, status)
        putExtra(ConsultActivity.titleKey, name)
    }

    @get:Rule
    internal val activityRule = ActivityScenarioRule<ConsultActivity>(intent)

    @Test
    fun testConsultActivity() {
        ConsultScreen {
            consultImage {
                isVisible()
                hasTag(avatarUrl)
            }
            consultTitle {
                isVisible()
                hasText(name)
            }
            consultStatus {
                isVisible()
                hasText(status)
            }
        }
    }
}
