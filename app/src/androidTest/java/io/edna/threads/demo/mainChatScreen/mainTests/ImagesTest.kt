package io.edna.threads.demo.mainChatScreen.mainTests

import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.PerformException
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import io.edna.threads.demo.BaseTestCase
import io.edna.threads.demo.appCode.activity.MainActivity
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImagesTest : BaseTestCase() {
    private val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<MainActivity>(intent)

    @Rule
    @JvmField
    val rule = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        GrantPermissionRule.grant(
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
        )
    } else {
        null
    }

    init {
        applyDefaultUserToDemoApp()
        prepareWsMocks()
    }

    @Test
    fun sendImage() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()

        ChatMainScreen {
            addAttachmentBtn {
                isVisible()
                isClickable()
                click()
            }
            val isRecyclerHasItems = try {
                bottomGalleryRecycler.getSize() > 0
            } catch (exc: PerformException) {
                false
            }
            if (isRecyclerHasItems.not()) {
                copyFileToDownloads("test_files/test_image2.jpg")
                Thread.sleep(500)
                pressBack()
                addAttachmentBtn { click() }
            }
            bottomGalleryRecycler {
                firstChild<ChatMainScreen.BottomGalleryItem> { click() }
            }
            sendImageBtn {
                isClickable()
                click()
            }
            assert(chatItemsRecyclerView.getSize() > 0)
        }
    }
}
