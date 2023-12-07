package im.threads.business.utils

import android.content.ClipboardManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import im.threads.business.core.ContextHolder
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.serviceLocator.core.inject
import im.threads.business.serviceLocator.core.startEdnaLocator
import im.threads.business.serviceLocator.coreSLModule
import im.threads.ui.serviceLocator.uiSLModule
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ClibpboardExtensionsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun before() {
        ContextHolder.context = context
        startEdnaLocator { modules(coreSLModule, uiSLModule) }
    }

    @Test
    fun whenCopyToBuffer_thenItExistsInBuffer() {
        val what = "test"
        val preferences: Preferences by inject()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.copyToBuffer(what)
        assert(clipboard.primaryClip?.getItemAt(0)?.text == what)
        assert(preferences.get<String>(PreferencesCoreKeys.LAST_COPY_TEXT) == what)
    }

    @Test
    fun whenCopyToBuffer_thenLastCopyTextIsValid() {
        val what = "test2"
        val preferences: Preferences by inject()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.copyToBuffer(what)
        assert(what.isLastCopyText())
    }
}
