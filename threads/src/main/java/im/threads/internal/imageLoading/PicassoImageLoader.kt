package im.threads.internal.imageLoading

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.exifinterface.media.ExifInterface
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.Target
import com.squareup.picasso.Transformation
import im.threads.internal.Config
import java.util.concurrent.Executors

class PicassoImageLoader : ImageLoaderRealisation {
    private val tag = PicassoImageLoader::class.java.simpleName
    private var loader: Picasso? = null

    override fun load(config: ImageLoader.Config) {
        val request = getImageRequestBuilder(config)
        if (config.callback != null) {
            request?.into(getPicassoTarget(config))
        } else {
            request?.into(config.imageView)
        }
    }

    override fun getBitmap(config: ImageLoader.Config) {
        if (config.callback != null) {
            getImageRequestBuilder(config)?.into(getPicassoTarget(config))
        }
    }

    override fun getBitmapSync(config: ImageLoader.Config): Bitmap? {
        return getImageRequestBuilder(config)?.get()
    }

    private fun getImageRequestBuilder(config: ImageLoader.Config): RequestCreator? {
        var builder: RequestCreator? = null
        config.url?.let {
            builder = getLoader(config.context).load(it)
            if (config.autoRotateWithExif) {
                builder!!.rotate(getRightAngleImage(it))
            }
        }
        config.resourceId?.let {
            builder = getLoader(config.context).load(it)
        }
        config.file?.let {
            builder = getLoader(config.context).load(it)
            if (config.autoRotateWithExif) {
                builder!!.rotate(getRightAngleImage(it.absolutePath))
            }
        }
        config.errorDrawableResourceId?.let { builder?.error(it) }
        config.modifications?.let {
            builder?.transform(getTransformations(it.toList()))
        }
        config.scales?.forEach {
            when (it) {
                ImageView.ScaleType.FIT_XY,
                ImageView.ScaleType.FIT_START,
                ImageView.ScaleType.FIT_CENTER,
                ImageView.ScaleType.FIT_END -> builder?.fit()
                ImageView.ScaleType.CENTER,
                ImageView.ScaleType.CENTER_INSIDE -> builder?.centerInside()
                ImageView.ScaleType.CENTER_CROP -> builder?.centerCrop()
                else -> {}
            }
        }

        return builder
    }

    private fun getTransformations(
        transformations: List<ImageModifications>
    ): List<Transformation> {
        val result = ArrayList<Transformation>(transformations.size)
        transformations.forEach {
            when (it) {
                is ImageModifications.CircleCropModification -> {
                    result.add(CircleTransformation())
                }
                is ImageModifications.MaskedModification -> {
                    result.add(MaskedTransformation(it.maskDrawable))
                }
            }
        }
        return result
    }

    private fun getPicassoTarget(config: ImageLoader.Config): Target {
        return object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                config.imageView?.setImageBitmap(bitmap)
                config.callback?.onImageLoaded(bitmap)
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                config.imageView?.setImageDrawable(errorDrawable)
                config.callback?.onImageLoadError()
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable) {}
        }
    }

    private fun getLoader(context: Context): Picasso {
        if (loader == null) {
            val builder = Picasso.Builder(Config.instance.context)
                .executor(Executors.newCachedThreadPool())

            ImageLoaderOkHttpProvider.okHttpClient?.let {
                builder.downloader(OkHttp3Downloader(it))
            }

            loader = builder.build()
        }
        return loader!!
    }

    private fun getRightAngleImage(photoPath: String): Float {
        return try {
            val ei = ExifInterface(Config.instance.context.contentResolver.openInputStream(Uri.parse(photoPath))!!)
            when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_NORMAL -> 0f
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                ExifInterface.ORIENTATION_UNDEFINED -> 0f
                else -> 90f
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            0f
        }
    }
}
