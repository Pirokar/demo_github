package io.edna.threads.demo

import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.rule.GrantPermissionRule
import io.edna.threads.demo.appCode.activity.MainActivity
import org.junit.Rule

open class BaseFilesTestCase : BaseTestCase() {
    private val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<MainActivity>(intent)

    init {
        applyDefaultUserToDemoApp()
        prepareWsMocks()
    }

    @Rule
    @JvmField
    val storageApiBelow29Rule = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        GrantPermissionRule.grant(
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
        )
    } else {
        null
    }

    @Rule
    @JvmField
    val storageApi29Rule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        GrantPermissionRule.grant(
            "android.permission.READ_MEDIA_IMAGES",
            "android.permission.READ_MEDIA_VIDEO",
            "android.permission.READ_MEDIA_AUDIO",
            "android.permission.ACCESS_MEDIA_LOCATION"
        )
    } else {
        null
    }
}
