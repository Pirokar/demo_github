package im.threads.internal.image_loading

import android.widget.ImageView

// TODO: rewrite it with one fun when everything will be in kotlin
internal fun ImageView.loadUrl(
    imageUrl: String?,
    scale: ImageScale? = null,
    errorDrawableResId: Int? = null,
    transformations: List<ImageModifications>? = null,
    callback: ImageLoader.ImageLoaderCallback? = null
) {
    if (!transformations.isNullOrEmpty()) {
        ImageLoaderExtensionsHelper.Companion.imageLoader.loadWithModifications(
            this,
            imageUrl,
            scale,
            errorDrawableResId,
            transformations,
            callback
        )
    } else if (callback != null) {
        ImageLoaderExtensionsHelper.Companion.imageLoader.loadImageWithCallback(
            this,
            imageUrl,
            scale,
            errorDrawableResId,
            callback
        )
    } else {
        ImageLoaderExtensionsHelper.Companion.imageLoader.loadImage(
            this,
            imageUrl,
            scale,
            errorDrawableResId
        )
    }
}

internal class ImageLoaderExtensionsHelper() {
    companion object {
        val imageLoader: ImageLoader by lazy {
            CoilImageLoader()
        }
    }
}
