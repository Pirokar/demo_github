package im.threads.internal.image_loading

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import coil.executeBlocking
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import coil.transform.Transformation
import java.io.File

class CoilImageLoader : ImageLoader {
    private val tag = "ImageLoader"
    private var coilImageLoader: coil.ImageLoader? = null

    override fun loadImage(
        imageView: ImageView,
        imageUrl: String?,
        scales: List<ImageView.ScaleType>?,
        errorDrawableResId: Int?
    ) {
        val request = getImageRequestBuilder(
            imageView,
            imageView.context,
            imageUrl,
            scales = scales,
            errorDrawableResId = errorDrawableResId
        ).build()
        getCoil(imageView.context).enqueue(request)
    }

    override fun loadFile(
        imageView: ImageView,
        file: File,
        scales: List<ImageView.ScaleType>?,
        errorDrawableResId: Int?,
        modifications: List<ImageModifications>?
    ) {
        val request = getImageRequestBuilder(
            imageView,
            imageView.context,
            file = file,
            scales = scales,
            errorDrawableResId = errorDrawableResId
        ).build()
        getCoil(imageView.context).enqueue(request)
    }

    override fun loadImageWithCallback(
        imageView: ImageView,
        imageUrl: String?,
        scales: List<ImageView.ScaleType>?,
        errorDrawableResId: Int?,
        callback: ImageLoader.ImageLoaderCallback
    ) {
        val request = getImageRequestBuilder(
            imageView,
            imageView.context,
            imageUrl,
            scales = scales,
            errorDrawableResId = errorDrawableResId,
            callback = callback
        ).build()
        getCoil(imageView.context).enqueue(request)
    }

    override fun loadWithModifications(
        imageView: ImageView,
        imageUrl: String?,
        scales: List<ImageView.ScaleType>?,
        errorDrawableResId: Int?,
        modifications: List<ImageModifications>,
        callback: ImageLoader.ImageLoaderCallback?
    ) {
        val request = getImageRequestBuilder(
            imageView,
            imageView.context,
            imageUrl,
            scales = scales,
            errorDrawableResId = errorDrawableResId,
            callback = callback
        )
            .transformations(getCoilTransformations(modifications))
            .build()
        getCoil(imageView.context).enqueue(request)
    }

    override fun getBitmap(
        context: Context,
        imageUrl: String?
    ): Bitmap? {
        val request = getImageRequestBuilder(
            context = context,
            imageUrl = imageUrl
        ).build()
        val drawable = getCoil(context).executeBlocking(request).drawable
        return getBitmap(drawable)
    }

    override fun getBitmap(
        context: Context,
        imageUrl: String?,
        modifications: List<ImageModifications>?
    ): Bitmap? {
        val request = getImageRequestBuilder(
            context = context,
            imageUrl = imageUrl
        )
        modifications?.let { request.transformations(getCoilTransformations(it)) }
        val drawable = getCoil(context).executeBlocking(request.build()).drawable
        return getBitmap(drawable)
    }

    override fun getDrawableAsync(
        context: Context,
        imageUrl: String?,
        modifications: List<ImageModifications>?,
        callback: ImageLoader.ImageLoaderCallback
    ) {
        val request = getImageRequestBuilder(
            context = context,
            imageUrl = imageUrl,
            callback = callback
        )
        modifications?.let { request.transformations(getCoilTransformations(it)) }
        getCoil(context).enqueue(request.build())
    }

    override fun getBitmapFromResource(
        context: Context,
        resourceId: Int,
        modifications: List<ImageModifications>?
    ): Bitmap? {
        val request = getImageRequestBuilder(
            context = context,
            resourceId = resourceId
        )
        modifications?.let { request.transformations(getCoilTransformations(modifications)) }
        val drawable = getCoil(context).executeBlocking(request.build()).drawable
        return getBitmap(drawable)
    }

    private fun getImageRequestBuilder(
        imageView: ImageView? = null,
        context: Context,
        imageUrl: String? = null,
        resourceId: Int? = null,
        file: File? = null,
        scales: List<ImageView.ScaleType>? = null,
        errorDrawableResId: Int? = null,
        callback: ImageLoader.ImageLoaderCallback? = null,
    ): ImageRequest.Builder {
        val builder = ImageRequest.Builder(context)

        imageUrl?.let { builder.data(it) }
        resourceId?.let { builder.data(it) }
        file?.let { builder.data(it) }
        errorDrawableResId?.let { builder.error(it) }
        builder.target(
            onError = {
                callback?.onImageLoadError()
            },
            onSuccess = {
                callback?.onImageLoaded(it)
                try {
                    imageView?.setImageDrawable(it)
                    scales?.let { scales ->
                        scales.forEach { scale ->
                            imageView?.scaleType = scale
                        }
                    }
                } catch (exc: Exception) {
                    Log.e(tag, "Error when trying to apply downloaded drawable", exc)
                }
            }
        )

        return builder
    }

    private fun getCoil(context: Context): coil.ImageLoader {
        return if (coilImageLoader == null) {
            val builder = coil.ImageLoader.Builder(context)
            ImageLoaderOkHttpProvider.okHttpClient?.let {
                builder.okHttpClient { it }
            }
            coilImageLoader = builder.build()
            coilImageLoader!!
        } else {
            coilImageLoader!!
        }
    }

    private fun getCoilTransformations(
        transformations: List<ImageModifications>
    ): List<Transformation> {
        val result = ArrayList<Transformation>(transformations.size)
        transformations.forEach {
            when (it) {
                is ImageModifications.CircleCropModification -> {
                    result.add(CircleCropTransformation())
                }
                is ImageModifications.MaskedModification -> {
                    result.add(MaskedTransformation(it.maskDrawable))
                }
            }
        }
        return result
    }

    private fun getBitmap(source: Drawable?) = source?.let { (it as BitmapDrawable).bitmap }
}
