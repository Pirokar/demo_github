package im.threads.business.imageLoading

object CurrentImageLoader {
    private var imageLoader: ImageLoaderRealisation? = PicassoImageLoader()

    fun getImageLoader(): ImageLoaderRealisation {
        if (imageLoader == null) {
            imageLoader = PicassoImageLoader()
        }

        return imageLoader ?: PicassoImageLoader()
    }

    fun clearLoader() {
        imageLoader = null
    }
}
