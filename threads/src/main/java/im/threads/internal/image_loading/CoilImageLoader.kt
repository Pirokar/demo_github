package im.threads.internal.image_loading

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import coil.executeBlocking
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import coil.transform.Transformation

class CoilImageLoader : ImageLoaderRealisation {
    private val tag = CoilImageLoader::class.java.simpleName
    private var coilImageLoader: coil.ImageLoader? = null

    override fun load(config: ImageLoader.Config) {
        val request = getImageRequestBuilder(config).build()
        getCoil(config.context).enqueue(request)
    }

    override fun getBitmapSync(config: ImageLoader.Config): Bitmap? {
        val request = getImageRequestBuilder(config).build()
        val drawable = getCoil(config.context).executeBlocking(request).drawable
        return getBitmap(drawable)
    }

    private fun getImageRequestBuilder(config: ImageLoader.Config): ImageRequest.Builder {
        val builder = ImageRequest.Builder(config.context)

        config.url?.let { builder.data(it) }
        config.resourceId?.let { builder.data(it) }
        config.file?.let { builder.data(it) }
        config.errorDrawableResourceId?.let { builder.error(it) }
        config.modifications?.let { builder.transformations(getCoilTransformations(it.toList())) }

        builder.target(
            onError = {
                config.callback?.onImageLoadError()
            },
            onSuccess = {
                config.callback?.onImageLoaded(it)
                try {
                    config.imageView?.setImageDrawable(it)
                    config.scales?.let { scales ->
                        scales.forEach { scale ->
                            config.imageView?.scaleType = scale
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
