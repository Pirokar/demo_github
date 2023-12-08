package im.threads.business.imageLoading

import androidx.test.core.app.ApplicationProvider
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class ImageRequestBuilderTest {
    private val imageRequestBuilder = ImageRequestBuilder()
    private val config = mock(ImageLoader.Config::class.java)

    @Before
    fun before() {
        `when`(config.context).thenReturn(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun whenGetImageRequestBuilderWithUrl_thenItReturnRequestBuilder() {
        `when`(config.url).thenReturn("https://testurl.com")
        `when`(config.resourceId).thenReturn(null)
        `when`(config.errorDrawableResourceId).thenReturn(null)

        val requestCreator = imageRequestBuilder.getImageRequestBuilder(config)
        assertNotNull(requestCreator)
    }

    @Test
    fun whenGetImageRequestBuilderWithFile_thenItReturnRequestBuilder() {
        `when`(config.errorDrawableResourceId).thenReturn(null)
        `when`(config.file).thenReturn(File("testfile"))
        `when`(config.resourceId).thenReturn(null)

        val requestCreator = imageRequestBuilder.getImageRequestBuilder(config)

        assertNotNull(requestCreator)
    }

    @Test
    fun whenGetImageRequestBuilderWithResourceId_thenItReturnRequestBuilder() {
        `when`(config.resourceId).thenReturn(im.threads.R.drawable.ecc_attach_file_grey_48x48)
        `when`(config.errorDrawableResourceId).thenReturn(null)

        val requestCreator = imageRequestBuilder.getImageRequestBuilder(config)

        assertNotNull(requestCreator)
    }

    @Test
    fun whenGetImageRequestBuilderWithNullInputs_thenItReturnNull() {
        `when`(config.resourceId).thenReturn(null)
        `when`(config.errorDrawableResourceId).thenReturn(null)

        val requestCreator = imageRequestBuilder.getImageRequestBuilder(config)

        assertNull(requestCreator)
    }
}
