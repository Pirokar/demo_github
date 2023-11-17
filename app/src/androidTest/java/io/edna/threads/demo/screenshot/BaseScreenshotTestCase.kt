package io.edna.threads.demo.screenshot

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import dev.testify.ScreenshotRule
import io.edna.threads.demo.appCode.activity.MainActivity
import io.edna.threads.demo.appCode.business.StringsProvider
import io.edna.threads.demo.assert
import io.edna.threads.demo.kaspressoSreens.DemoLoginScreen
import io.edna.threads.demo.kaspressoSreens.DemoSamplesListView
import org.junit.Rule

// https://github.com/ndtp/android-testify
open class BaseScreenshotTestCase() {
    private val context: Context = ApplicationProvider.getApplicationContext()
    protected val stringsProvider = StringsProvider(context)

    @get:Rule
    val screenshotRule = ScreenshotRule(MainActivity::class.java)

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

    protected fun openDemoExample(exampleTextInList: String) {
        DemoLoginScreen {
            demoButton {
                assert("Кнопка демо примеров должна быть видимой и кликабельной", ::isVisible, ::isClickable)
                click()
            }
        }
        DemoSamplesListView {
            sampleListRecyclerView {
                assert("Список демо примеров должен быть видимый") { isVisible() }
                val child = childWith<DemoSamplesListView.SampleListItem> {
                    withDescendant {
                        withText(exampleTextInList)
                    }
                }
                child {
                    descriptionTextView {
                        assert("Текст демо примера: \"$exampleTextInList\" должен быть кликабельный") {
                            isClickable()
                        }
                        click()
                    }
                }
            }
        }
    }
}
