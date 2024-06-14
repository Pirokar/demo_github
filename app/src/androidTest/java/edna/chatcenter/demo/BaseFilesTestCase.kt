package edna.chatcenter.demo

import android.content.Intent
import android.os.Environment
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import edna.chatcenter.demo.appCode.activity.MainActivity
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

    @After
    open override fun after() {
        super.after()

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
