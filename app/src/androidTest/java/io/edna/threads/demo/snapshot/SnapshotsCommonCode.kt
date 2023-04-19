package io.edna.threads.demo.snapshot

import androidx.test.platform.app.InstrumentationRegistry
import dev.testify.ScreenshotRule
import io.edna.threads.demo.appCode.test.TestChatActivity
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

internal fun saveJsonMock(resourceId: Int, rule: ScreenshotRule<TestChatActivity>) {
    var string: String? = ""
    val stringBuilder = StringBuilder()
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val inputStream: InputStream = context.resources.openRawResource(resourceId)
    val reader = BufferedReader(InputStreamReader(inputStream))
    while (true) {
        try {
            if (reader.readLine().also { string = it } == null) break
        } catch (e: IOException) {
            e.printStackTrace()
        }
        stringBuilder.append(string).append("\n")
    }
    inputStream.close()
    rule.addIntentExtras {
        it.putString(TestChatActivity.jsonMockExtraKey, stringBuilder.toString())
    }
}

internal fun providePermissions() {
    InstrumentationRegistry.getInstrumentation().uiAutomation.apply {
        executeShellCommand("pm grant im.threads android.permission.READ_EXTERNAL_STORAGE")
        executeShellCommand("pm grant im.threads android.permission.WRITE_EXTERNAL_STORAGE")
        executeShellCommand("pm grant im.threads android.permission.READ_MEDIA_IMAGES")
        executeShellCommand("pm grant im.threads android.permission.READ_MEDIA_VIDEO")
        executeShellCommand("pm grant im.threads android.permission.READ_MEDIA_AUDIO")
        executeShellCommand("pm grant im.threads android.permission.ACCESS_MEDIA_LOCATION")
    }
}
