package im.threads.internal.imageLoading

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import java.io.File

/**
 * Загрузчик изображений. Содержит поле currentImageLoader, которое определяет конкретную реализацию
 *  и config, который определяет параметры загрузки.
 */
class ImageLoader private constructor() {
    private val config = ImageLoader.Config()
    private val currentImageLoader: ImageLoaderRealisation = PicassoImageLoader()

    /**
     * Указывает url для загрузки
     * @param url строка, определяющая url.
     */
    fun load(url: String?): ImageLoader {
        config.url = url
        return this
    }

    /**
     * Указывает файл для загрузки
     * @param file файл, который необходимо загрузить
     */
    fun load(file: File): ImageLoader {
        config.file = file
        return this
    }

    /**
     * Указывает ресурс Drawable для загрузки
     * @param resourceId ресурс, который необходимо загрузить
     */
    fun load(resourceId: Int): ImageLoader {
        config.resourceId = resourceId
        return this
    }

    /**
     * Указывает ресурс Drawable, который будет отображаться в случае ошибки при загрузке
     * @param resourceId drawable ресурс
     */
    fun errorDrawableResourceId(resourceId: Int?): ImageLoader {
        config.errorDrawableResourceId = resourceId
        return this
    }

    /**
     * Указывает параметры ImageView.ScaleType, которые будут применены последовательно к изображению
     * @param scales укажите через запятую необходимые scales
     */
    fun scales(vararg scales: ImageView.ScaleType): ImageLoader {
        config.scales = scales
        return this
    }

    /**
     * Указывает параметры ImageView.ScaleType, которые будут применены последовательно к изображению
     * @param scales список scales
     */
    fun scales(scales: List<ImageView.ScaleType>?): ImageLoader {
        scales?.let {
            config.scales = it.toTypedArray()
        }
        return this
    }

    /**
     * Указывает модификации (трансформации), которые будут применены последовательно к изображению
     * @param modifications укажите через запятую необходимые модификации
     */
    fun modifications(vararg modifications: ImageModifications): ImageLoader {
        config.modifications = modifications
        return this
    }

    /**
     * Указывает модификации (трансформации), которые будут применены последовательно к изображению
     * @param modifications список модификаций
     */
    fun modifications(modifications: List<ImageModifications>?): ImageLoader {
        modifications?.let {
            config.modifications = it.toTypedArray()
        }
        return this
    }

    /**
     * Объект обратного вызова, который будет вызван после загрузки изображения или в случае ошибки
     * @param callback содержит два метода. onImageLoaded - в случае успешной загрузки,
     *  onImageLoadError - в случае ошибки
     */
    fun callback(callback: ImageLoaderCallback?): ImageLoader {
        config.callback = callback
        return this
    }

    /**
     * Указывает контейнер, в который необходимо загрузить изображение
     * @param imageView целевой контейнер
     */
    fun into(imageView: ImageView) {
        config.imageView = imageView
        config.context = imageView.context

        currentImageLoader.load(config)
    }

    /**
     * Загружает Bitmap асинхронно и возвращает его. Передайте коллбэк для результата
     * @param context android context
     */
    fun getBitmap(context: Context) {
        config.context = context
        return currentImageLoader.getBitmap(config)
    }

    /**
     * Загружает Bitmap синхронно и возвращает его.
     * @param context android context
     */
    fun getBitmapSync(context: Context): Bitmap? {
        config.context = context
        return currentImageLoader.getBitmapSync(config)
    }

    /**
     * Конфигурация для загрузки
     */
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
        fun onImageLoaded(bitmap: Bitmap) {}

        /**
         * Вызывается в случае ошибки при загрзуке изображения
         */
        fun onImageLoadError() {}
    }

    companion object {
        /**
         * Возвращает объект ImageLoader
         */
        @JvmStatic
        fun get() = ImageLoader()
    }
}
