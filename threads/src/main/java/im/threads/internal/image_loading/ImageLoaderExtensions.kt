package im.threads.internal.image_loading

import android.util.Log
import android.widget.ImageView
import java.io.File

internal fun ImageView.setImage(
    data: Any? = null,
    scales: List<ImageView.ScaleType>? = null,
    errorDrawableResId: Int? = null,
    modifications: List<ImageModifications>? = null,
    callback: ImageLoader.ImageLoaderCallback? = null
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
            ?.into(this)
            ?: Log.e("ImageLoading", "Data is empty, nothing to load")
    }
}
