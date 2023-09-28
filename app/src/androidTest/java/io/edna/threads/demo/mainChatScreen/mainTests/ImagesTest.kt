package io.edna.threads.demo.mainChatScreen.mainTests

import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import io.edna.threads.demo.BaseTestCase
import io.edna.threads.demo.appCode.activity.MainActivity
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
        copyImageToDownloads("test_files/test_image2.jpg")
    }

    private fun copyImageToDownloads(assetsPath: String) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            copyToDownloadsApiBelow29(assetsPath)
        } else {
            copyToDownloadsApi29(assetsPath)
        }
    }
}
