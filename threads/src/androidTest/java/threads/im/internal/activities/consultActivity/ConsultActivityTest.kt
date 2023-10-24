package threads.im.internal.activities.consultActivity

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import im.threads.business.models.ConsultInfo
import im.threads.ui.activities.ConsultActivity
import im.threads.ui.config.ConfigBuilder
import im.threads.ui.core.ThreadsLib
import org.junit.Rule
import org.junit.Test

class ConsultActivityTest : TestCase() {
    private val avatarUrl = "https://img.freepik.com/free-photo/pretty-smiling-joyfully-female-with-fair-hair-dressed-casually-looking-with-satisfaction_176420-15187.jpg?w=826&t=st=1660821591~exp=1660822191~hmac=03ad70b2616db9df41c6174265f7a97806338958ef738236badc63c5b0ff1897"
    private val status = "Active"
    private val name = "Operator Alisa"

    init {
        val configBuilder = ConfigBuilder(ApplicationProvider.getApplicationContext())
            .surveyCompletionDelay(2000)
            .historyLoadingCount(50)
            .isDebugLoggingEnabled(true)
            .showAttachmentsButton()
            .serverBaseUrl("http://ednatest.ru")
            .datastoreUrl("http://ednatest.ru")
            .threadsGateUrl("wss://ednatest.ru")
            .threadsGateProviderUid("threadsGateProviderUid")
            .setNewChatCenterApi()

        ThreadsLib.init(configBuilder as ConfigBuilder)
    }

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
