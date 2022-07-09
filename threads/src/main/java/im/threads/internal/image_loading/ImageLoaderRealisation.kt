package im.threads.internal.image_loading

import android.graphics.Bitmap

/**
 * Контракт для конкретного загрузчика изображения.
 */
interface ImageLoaderRealisation {
    /**
     * Загружает изображение согласно переданной конфигурации
     * @param config конфигурация для загрузки
     */
    fun load(config: ImageLoader.Config)

    /**
     * Загружает Bitmap синхронно согласно переданной конфигурации
     * @param config конфигурация для загрузки
     */
    fun getBitmapSync(config: ImageLoader.Config): Bitmap?
}
