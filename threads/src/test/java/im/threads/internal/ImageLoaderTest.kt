package im.threads.internal

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import im.threads.internal.image_loading.ImageLoader
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ImageLoaderTest {
    private val imageLoader = ImageLoader.get()
    private val context = ApplicationProvider.getApplicationContext<Context>()
}
