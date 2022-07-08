package im.threads.internal.image_loading

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import java.io.File

// TODO: rewrite it with one fun when everything will be in kotlin
interface ImageLoader {
    fun loadImage(
        imageView: ImageView,
        imageUrl: String?,
        scales: List<ImageView.ScaleType>?,
        errorDrawableResId: Int?
    )

    fun loadFile(
        imageView: ImageView,
        file: File,
        scales: List<ImageView.ScaleType>?,
        errorDrawableResId: Int?,
        modifications: List<ImageModifications>?
    )

    fun loadImageWithCallback(
        imageView: ImageView,
        imageUrl: String?,
        scales: List<ImageView.ScaleType>?,
        errorDrawableResId: Int?,
        callback: ImageLoaderCallback
    )

    fun loadWithModifications(
        imageView: ImageView,
        imageUrl: String?,
        scales: List<ImageView.ScaleType>?,
        errorDrawableResId: Int?,
        modifications: List<ImageModifications>,
        callback: ImageLoaderCallback?
    )

    fun getBitmap(
        context: Context,
        imageUrl: String?
    ): Bitmap?

    fun getBitmap(
        context: Context,
        imageUrl: String?,
        modifications: List<ImageModifications>?
    ): Bitmap?

    fun getDrawableAsync(
        context: Context,
        imageUrl: String?,
        modifications: List<ImageModifications>?,
        callback: ImageLoaderCallback
    )

    fun getBitmapFromResource(
        context: Context,
        resourceId: Int,
        modifications: List<ImageModifications>?
    ): Bitmap?

    interface ImageLoaderCallback {
        fun onImageLoaded(drawable: Drawable) {}
        fun onImageLoadError() {}
    }
}
