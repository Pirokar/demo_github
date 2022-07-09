package im.threads.internal.image_loading

import android.graphics.Bitmap

/**
 * Контракт для конкретного загрузчика изображения.
 */
interface ImageLoaderRealisation {
    fun load(config: ImageLoader.Config)
    fun getBitmapSync(config: ImageLoader.Config): Bitmap?
}
