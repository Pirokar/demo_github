package im.threads.business.imageLoading

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MaskedTransformationTest {
    private val transformation = MaskedTransformation(getDrawable())

    @Test
    fun testTransform() {
        val source: Bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val expected: Bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)

        val result = transformation.transform(source)

        assert(expected.height == result.height && expected.width == result.width)
    }

    private fun getDrawable(): Drawable {
        val myBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        myBitmap.setPixel(0, 0, Color.BLUE)
        return BitmapDrawable(ApplicationProvider.getApplicationContext<Context>().resources, myBitmap)
    }
}
