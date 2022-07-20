package im.threads.internal.imageLoading

import android.util.Log
import android.widget.ImageView
import java.io.File

internal fun ImageView.loadImage(
    data: Any? = null,
    scales: List<ImageView.ScaleType>? = null,
    errorDrawableResId: Int? = null,
    modifications: List<ImageModifications>? = null,
    callback: ImageLoader.ImageLoaderCallback? = null,
    autoRotateWithExif: Boolean = false
) {
    data?.let {
        when (data) {
            is String -> ImageLoader.get().load(data)
            is File -> ImageLoader.get().load(data)
            is Int -> ImageLoader.get().load(data)
            else -> null
        }?.scales(scales)
            ?.errorDrawableResourceId(errorDrawableResId)
            ?.modifications(modifications)
            ?.callback(callback)
            ?.autoRotateWithExif(autoRotateWithExif)
            ?.into(this)
            ?: Log.e("ImageLoading", "Data is empty, nothing to load")
    }
}
