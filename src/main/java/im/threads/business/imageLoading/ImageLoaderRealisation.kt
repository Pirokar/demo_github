package im.threads.business.imageLoading

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
     * Загружает Bitmap согласно переданной конфигурации. Предоставьте callback для результата
     * @param config конфигурация для загрузки
     */
    fun getBitmap(config: ImageLoader.Config)

    /**
     * Загружает Bitmap согласно переданной конфигурации синхронно.
     * @param config конфигурация для загрузки
     */
    fun getBitmapSync(config: ImageLoader.Config): Bitmap?
}
