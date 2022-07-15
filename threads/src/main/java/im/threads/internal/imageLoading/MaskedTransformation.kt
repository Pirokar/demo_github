package im.threads.internal.imageLoading

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import android.graphics.drawable.NinePatchDrawable
import com.squareup.picasso.Transformation

/**
 * Преобразует изображение согласно переданной на вход маске
 * @param maskDrawable маска типа NinePatchDrawable
 */
class MaskedTransformation(private val maskDrawable: Drawable) : Transformation {
    override fun key(): String = maskDrawable.hashCode().toString()

    override fun transform(source: Bitmap): Bitmap? {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val mCanvas = Canvas(result)
        val o = BitmapFactory.Options()
        o.inSampleSize = 2
        val mask = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val tmpCanvas = Canvas(mask)
        val maskRaw9Patch = maskDrawable as NinePatchDrawable
        maskRaw9Patch.setBounds(0, 0, tmpCanvas.width, tmpCanvas.height)
        maskRaw9Patch.draw(tmpCanvas)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        mCanvas.drawBitmap(source, 0f, 0f, null)
        mCanvas.drawBitmap(mask, 0f, 0f, paint)
        paint.xfermode = null
        source.recycle()

        return result
    }
}
