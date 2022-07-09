package im.threads.internal.image_loading

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import java.io.File

class ImageLoader private constructor() {
    private val config = ImageLoader.Config()
    private val currentImageLoader: ImageLoaderRealisation = CoilImageLoader()

    fun load(url: String?): ImageLoader {
        config.url = url
        return this
    }

    fun load(file: File): ImageLoader {
        config.file = file
        return this
    }

    fun load(resourceId: Int): ImageLoader {
        config.resourceId = resourceId
        return this
    }

    fun errorDrawableResourceId(resourceId: Int?): ImageLoader {
        config.errorDrawableResourceId = resourceId
        return this
    }

    fun scales(vararg scales: ImageView.ScaleType): ImageLoader {
        config.scales = scales
        return this
    }

    fun scales(scales: List<ImageView.ScaleType>?): ImageLoader {
        scales?.let {
            config.scales = it.toTypedArray()
        }
        return this
    }

    fun modifications(vararg modifications: ImageModifications): ImageLoader {
        config.modifications = modifications
        return this
    }

    fun modifications(modifications: List<ImageModifications>?): ImageLoader {
        modifications?.let {
            config.modifications = it.toTypedArray()
        }
        return this
    }

    fun callback(callback: ImageLoaderCallback?): ImageLoader {
        config.callback = callback
        return this
    }

    fun into(imageView: ImageView) {
        config.imageView = imageView
        config.context = imageView.context

        currentImageLoader.load(config)
    }

    fun getBitmapSync(context: Context): Bitmap? {
        config.context = context
        return currentImageLoader.getBitmapSync(config)
    }

    fun getDrawableAsync(context: Context) {
        config.context = context
        return currentImageLoader.load(config)
    }

    class Config {
        lateinit var context: Context
        var url: String? = null
        var file: File? = null
        var resourceId: Int? = null
        var errorDrawableResourceId: Int? = null
        var scales: Array<out ImageView.ScaleType>? = null
        var modifications: Array<out ImageModifications>? = null
        var imageView: ImageView? = null
        var callback: ImageLoaderCallback? = null
    }

    interface ImageLoaderCallback {
        /**
         * Вызывается по окончанию загрзуки изображений
         * @param drawable загруженное изображение
         */
        fun onImageLoaded(drawable: Drawable) {}

        /**
         * Вызывается в случае ошибки при загрзуке изображения
         */
        fun onImageLoadError() {}
    }

    companion object {
        @JvmStatic
        fun get() = ImageLoader()
    }
}
