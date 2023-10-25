package im.threads.business.imageLoading

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PicassoImageLoader : ImageLoaderRealisation {
    private val requestBuilder = ImageRequestBuilder()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun load(config: ImageLoader.Config) {
        coroutineScope.launch {
            val request = withContext(Dispatchers.IO) { requestBuilder.getImageRequestBuilder(config) }
            if (config.callback != null) {
                request?.into(
                    config.imageView,
                    object : Callback {
                        override fun onSuccess() {
                            config.callback?.onImageLoaded()
                        }

                        override fun onError(e: java.lang.Exception?) {
                            config.callback?.onImageLoadError()
                        }
                    }
                )
            } else {
                request?.into(config.imageView)
            }
        }
    }

    override fun getBitmap(config: ImageLoader.Config) {
        if (config.callback != null) {
            coroutineScope.launch {
                val request = withContext(Dispatchers.IO) { requestBuilder.getImageRequestBuilder(config) }
                request?.into(getPicassoTarget(config))
            }
        }
    }

    override fun getBitmapSync(config: ImageLoader.Config): Bitmap? {
        return requestBuilder.getImageRequestBuilder(config)?.get()
    }

    private fun getPicassoTarget(config: ImageLoader.Config): Target {
        return object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                config.imageView?.setImageBitmap(bitmap)
                config.callback?.onBitmapLoaded(bitmap)
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                config.imageView?.setImageDrawable(errorDrawable)
                config.callback?.onImageLoadError()
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
        }
    }
}
