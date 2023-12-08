package im.threads.business.imageLoading

import android.content.Context
import android.util.Size
import android.widget.ImageView
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class ImageLoaderTest {
    private val imageLoader = ImageLoader.get()

    @Test
    fun testLoadUrl() {
        val url = "https://example.com/image.jpg"
        imageLoader.load(url)

        assertEquals(url, imageLoader.config.url)
    }

    @Test
    fun testLoadFile() {
        val file = File("test.jpg")
        imageLoader.load(file)

        assertEquals(file, imageLoader.config.file)
    }

    @Test
    fun testLoadResourceId() {
        val resourceId = 123
        imageLoader.load(resourceId)

        assertEquals(resourceId, imageLoader.config.resourceId)
    }

    @Test
    fun testErrorDrawableResourceId() {
        val resourceId = 456
        imageLoader.errorDrawableResourceId(resourceId)

        assertEquals(resourceId, imageLoader.config.errorDrawableResourceId)
    }

    @Test
    fun testModificationsVararg() {
        val mockModifications = arrayOf(
            mock(ImageModifications::class.java),
            mock(ImageModifications::class.java),
            mock(ImageModifications::class.java)
        )
        imageLoader.modifications(*mockModifications)
        assertArrayEquals(mockModifications, imageLoader.config.modifications)
    }

    @Test
    fun testModificationsList() {
        val mockModifications = listOf(
            mock(ImageModifications::class.java),
            mock(ImageModifications::class.java),
            mock(ImageModifications::class.java)
        )
        imageLoader.modifications(mockModifications)
        assertArrayEquals(mockModifications.toTypedArray(), imageLoader.config.modifications)
    }

    @Test
    fun testCallback() {
        val mockCallback = mock(ImageLoader.ImageLoaderCallback::class.java)
        imageLoader.callback(mockCallback)
        assertEquals(mockCallback, imageLoader.config.callback)
    }

    @Test
    fun testAutoRotateWithExif() {
        imageLoader.autoRotateWithExif(true)
        assert(imageLoader.config.isAutoRotateWithExif)
    }

    @Test
    fun testDisableEdnaSsl() {
        imageLoader.disableEdnaSsl()

        assert(!imageLoader.config.isImageUnderSsl)
    }

    @Test
    fun testResize() {
        val targetWidth = 300
        val targetHeight = 200
        imageLoader.resize(targetWidth, targetHeight)

        assertEquals(Size(targetWidth, targetHeight), imageLoader.config.resizePair)
    }

    @Test
    fun testOnlyScaleDown() {
        imageLoader.onlyScaleDown()

        assert(imageLoader.config.isOnlyScaleDown)
    }

    @Test
    fun testNoPlaceholder() {
        imageLoader.noPlaceholder()

        assert(imageLoader.config.noPlaceholder)
    }

    @Test
    fun testInto() {
        val mockImageView = mock(ImageView::class.java)
        val context = ApplicationProvider.getApplicationContext<Context>()
        Mockito.`when`(mockImageView.context).thenReturn(context)

        imageLoader.into(mockImageView)

        assertEquals(mockImageView, imageLoader.config.imageView)
        assertEquals(context, imageLoader.config.context)
    }
}
