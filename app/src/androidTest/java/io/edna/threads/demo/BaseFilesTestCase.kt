package io.edna.threads.demo

import android.content.Intent
import android.os.Build
import android.os.Environment
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.rule.GrantPermissionRule
import io.edna.threads.demo.appCode.activity.MainActivity
import org.junit.After
import org.junit.Rule
import java.io.File

open class BaseFilesTestCase : BaseTestCase() {
    protected val fileNamesToDelete = arrayListOf<String>()

    private val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<MainActivity>(intent)

    init {
        applyDefaultUserToDemoApp()
        prepareWsMocks()
    }

    @Rule
    @JvmField
    val storageApiBelow29Rule = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        GrantPermissionRule.grant(
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
        )
    } else {
        null
    }

    @Rule
    @JvmField
    val storageApi29Rule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        GrantPermissionRule.grant(
            "android.permission.READ_MEDIA_IMAGES",
            "android.permission.READ_MEDIA_VIDEO",
            "android.permission.READ_MEDIA_AUDIO",
            "android.permission.ACCESS_MEDIA_LOCATION",
            "android.permission.RECORD_AUDIO"
        )
    } else {
        null
    }

    @After
    open fun after() {
        if (fileNamesToDelete.isNotEmpty()) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.absolutePath?.let { downloadsPath ->
                fileNamesToDelete.forEach {
                    val toFile = File("$downloadsPath/$it")
                    if (toFile.exists()) {
                        toFile.delete()
                    }
                }
            }
        }
    }
}
