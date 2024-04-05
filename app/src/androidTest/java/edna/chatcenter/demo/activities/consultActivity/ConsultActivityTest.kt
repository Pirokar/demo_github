package edna.chatcenter.demo.activities.consultActivity

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import edna.chatcenter.demo.BaseTestCase
import edna.chatcenter.ui.core.models.ConsultInfo
import edna.chatcenter.ui.visual.activities.ConsultActivity
import org.junit.Rule
import org.junit.Test

class ConsultActivityTest : BaseTestCase() {
    private val avatarUrl = "https://img.freepik.com/free-photo/pretty-smiling-joyfully-female-with-fair-hair-dressed-casually-looking-with-satisfaction_176420-15187.jpg?w=826&t=st=1660821591~exp=1660822191~hmac=03ad70b2616db9df41c6174265f7a97806338958ef738236badc63c5b0ff1897"
    private val status = "Active"
    private val name = "Operator Alisa"

    private val intent = Intent(ApplicationProvider.getApplicationContext(), ConsultActivity::class.java).apply {
        putExtra(
            ConsultActivity.consultInfoKey,
            ConsultInfo(
                photoUrl = avatarUrl,
                status = status,
                name = name
            )
        )
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
