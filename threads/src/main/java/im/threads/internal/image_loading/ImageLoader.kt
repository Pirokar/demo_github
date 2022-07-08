package im.threads.internal.image_loading

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import java.io.File

// TODO: rewrite it with one fun when everything will be in kotlin
/**
 * Загрузчик изображений
 */
interface ImageLoader {
    /**
     * Загружает изображение асинхронно
     * @param imageView контейнер, в который необходимо загрузить изображение
     * @param imageUrl url, который представляет изображение
     * @param scales список Scale, которые применятся последовательно к ImageView
     * @param errorDrawableResId ссылка на ресурс Drawable, который будет отображен в случае ошибки
     * загрузки (напр., R.drawable.error_example)
     */
    fun loadImage(
        imageView: ImageView,
        imageUrl: String?,
        scales: List<ImageView.ScaleType>?,
        errorDrawableResId: Int?
    )

    /**
     * Загружает изображение асинхронно
     * @param imageView контейнер, в который необходимо загрузить изображение
     * @param file файл, который представляет изображение
     * @param scales список Scale, которые применятся последовательно к ImageView
     * @param errorDrawableResId ссылка на ресурс Drawable, который будет отображен в случае ошибки
     * загрузки (напр., R.drawable.error_example)
     * @param modifications Список трансофрмаций, которые будут последовательно применены к ImageView
     */
    fun loadFile(
        imageView: ImageView,
        file: File,
        scales: List<ImageView.ScaleType>?,
        errorDrawableResId: Int?,
        modifications: List<ImageModifications>?
    )

    /**
     * Загружает изображение асинхронно
     * @param imageView контейнер, в который необходимо загрузить изображение
     * @param imageUrl url, который представляет изображение
     * @param scales список Scale, которые применятся последовательно к ImageView
     * @param errorDrawableResId ссылка на ресурс Drawable, который будет отображен в случае ошибки
     * загрузки (напр., R.drawable.error_example)
     * @param callback будет вызван, когда изображение загрузится или случится ошибка при загрузке
     */
    fun loadImageWithCallback(
        imageView: ImageView,
        imageUrl: String?,
        scales: List<ImageView.ScaleType>?,
        errorDrawableResId: Int?,
        callback: ImageLoaderCallback
    )

    /**
     * Загружает изображение асинхронно
     * @param imageView контейнер, в который необходимо загрузить изображение
     * @param imageUrl url, который представляет изображение
     * @param scales список Scale, которые применятся последовательно к ImageView
     * @param errorDrawableResId ссылка на ресурс Drawable, который будет отображен в случае ошибки
     * загрузки (напр., R.drawable.error_example)
     * @param modifications список трансофрмаций, которые будут последовательно применены к ImageView
     * @param callback будет вызван, когда изображение загрузится или случится ошибка при загрузке
     */
    fun loadWithModifications(
        imageView: ImageView,
        imageUrl: String?,
        scales: List<ImageView.ScaleType>?,
        errorDrawableResId: Int?,
        modifications: List<ImageModifications>,
        callback: ImageLoaderCallback?
    )

    /**
     * Загружает Bitmap из url синхронно
     * @param context контекст Android
     * @param imageUrl url, который представляет изображение
     */
    fun getBitmap(
        context: Context,
        imageUrl: String?
    ): Bitmap?

    /**
     * Загружает Bitmap из url синхронно
     * @param context контекст Android
     * @param imageUrl url, который представляет изображение
     * @param modifications список трансофрмаций, которые будут последовательно применены к Bitmap
     */
    fun getBitmap(
        context: Context,
        imageUrl: String?,
        modifications: List<ImageModifications>?
    ): Bitmap?

    /**
     * Получает Drawable из url асинхронно
     * @param context контекст Android
     * @param imageUrl url, который представляет изображение
     * @param modifications список трансофрмаций, которые будут последовательно применены к Bitmap
     * @param callback будет вызван, когда изображение загрузится или случится ошибка при загрузке
     */
    fun getDrawableAsync(
        context: Context,
        imageUrl: String?,
        modifications: List<ImageModifications>?,
        callback: ImageLoaderCallback
    )

    /**
     * Получает Bitmap из переданной ссылки на ресурс изображения
     * @param context контекст Android
     * @param resourceId ссылка на ресурс изображения (напр., R.drawable.image_example)
     * @param modifications список трансофрмаций, которые будут последовательно применены к Bitmap
     */
    fun getBitmapFromResource(
        context: Context,
        resourceId: Int,
        modifications: List<ImageModifications>?
    ): Bitmap?

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
}
