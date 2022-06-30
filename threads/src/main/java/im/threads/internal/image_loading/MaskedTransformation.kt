package im.threads.internal.image_loading

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import android.graphics.drawable.NinePatchDrawable
import coil.size.Size
import coil.transform.Transformation

/**
 * Преобразует изображение согласно переданной на вход маске
 * @param maskDrawable маска типа NinePatchDrawable
 */
class MaskedTransformation(private val maskDrawable: Drawable) : Transformation {
    override val cacheKey: String = maskDrawable.hashCode().toString()

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val result = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        BitmapFactory.Options().inSampleSize = 2
        val mask = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
        val tmpCanvas = Canvas(mask)
        val maskRaw9Patch = maskDrawable as NinePatchDrawable
        maskRaw9Patch.setBounds(0, 0, tmpCanvas.width, tmpCanvas.height)
        maskRaw9Patch.draw(tmpCanvas)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        canvas.drawBitmap(input, 0f, 0f, null)
        canvas.drawBitmap(mask, 0f, 0f, paint)
        paint.xfermode = null
        input.recycle()

        return result
    }
}
