package im.threads.business.transform

import android.graphics.Bitmap
import im.threads.business.imageLoading.CircleTransformation
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CircleTransformationTest {
    private val circleTransformation = CircleTransformation()

    @Test
    fun whenCallTransformBitmap_thenItSizesAreCorrect() {
        val source = Mockito.mock(Bitmap::class.java)
        Mockito.`when`(source.width).thenReturn(100)
        Mockito.`when`(source.height).thenReturn(100)
        Mockito.`when`(source.config).thenReturn(Bitmap.Config.ARGB_8888)

        val transformed = circleTransformation.transform(source)

        assertEquals(100, transformed.width)
        assertEquals(100, transformed.height)
    }

    @Test
    fun whenCallingKeyFromTransform_thenItCircle() {
        assertEquals("circle", circleTransformation.key())
    }
}
