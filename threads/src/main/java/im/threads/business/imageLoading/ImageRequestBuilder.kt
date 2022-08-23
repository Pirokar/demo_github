package im.threads.business.imageLoading

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import androidx.exifinterface.media.ExifInterface
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.Transformation
import im.threads.internal.Config
import java.util.concurrent.Executors

class ImageRequestBuilder {
    private var sslImagesLoader: Picasso? = null
    private var pureImagesLoader: Picasso? = null

    fun getImageRequestBuilder(
        config: ImageLoader.Config
    ): RequestCreator? {
        var builder: RequestCreator? = null
        config.url?.let {
            builder = getLoader(config).load(it)

            if (config.isAutoRotateWithExif) {
                builder!!.rotate(getRightAngleImage(it))
            }
        }
        config.resourceId?.let {
            builder = getLoader(config).load(it)
        }
        config.file?.let {
            builder = getLoader(config).load(it)
            if (config.isAutoRotateWithExif) {
                builder!!.rotate(getRightAngleImage(it.absolutePath))
            }
        }
        config.errorDrawableResourceId?.let { builder?.error(it) }
        config.resizePair?.let {
            builder?.resize(it.first, it.second)
        }
        if (config.isOnlyScaleDown) {
            builder?.onlyScaleDown()
        }
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

        return if (config.url == null && config.file == null && config.resourceId == null) {
            null
        } else {
            builder
        }
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

    private fun getLoader(config: ImageLoader.Config): Picasso {
        return if (config.isImageUnderSsl) {
            getSslImagesLoader(config.context)
        } else {
            getPureImagesLoader(config.context)
        }
    }

    private fun getSslImagesLoader(context: Context): Picasso {
        if (sslImagesLoader == null) {
            val builder = Picasso.Builder(context)
                .executor(Executors.newCachedThreadPool())

            ImageLoaderOkHttpProvider.okHttpClient?.let {
                builder.downloader(OkHttp3Downloader(it))
            }

            sslImagesLoader = builder.build()
        }
        return sslImagesLoader!!
    }

    private fun getPureImagesLoader(context: Context): Picasso {
        if (pureImagesLoader == null) {
            val builder = Picasso.Builder(context)
                .executor(Executors.newCachedThreadPool())

            pureImagesLoader = builder.build()
        }
        return pureImagesLoader!!
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
