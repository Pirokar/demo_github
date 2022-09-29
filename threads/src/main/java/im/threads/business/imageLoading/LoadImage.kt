package im.threads.business.imageLoading

import android.widget.ImageView
import im.threads.business.logger.core.LoggerEdna
import java.io.File

internal fun ImageView.loadImage(
    data: Any? = null,
    scales: List<ImageView.ScaleType>? = null,
    errorDrawableResId: Int? = null,
    modifications: List<ImageModifications>? = null,
    callback: ImageLoader.ImageLoaderCallback? = null,
    autoRotateWithExif: Boolean = false,
    isExternalImage: Boolean = false
) {
    data?.let {
        val builder = when (data) {
            is String -> ImageLoader.get().load(data)
            is File -> ImageLoader.get().load(data)
            is Int -> ImageLoader.get().load(data)
            else -> null
        }
        builder?.let { builder ->
            if (isExternalImage) {
                builder.disableEdnaSsl()
            }

            builder.scales(scales)
                .errorDrawableResourceId(errorDrawableResId)
                .modifications(modifications)
                .callback(callback)
                .autoRotateWithExif(autoRotateWithExif)
                .into(this)
        } ?: showLog()
    } ?: showLog()
}

private fun showLog() {
    LoggerEdna.error("Data is empty, nothing to load")
}
