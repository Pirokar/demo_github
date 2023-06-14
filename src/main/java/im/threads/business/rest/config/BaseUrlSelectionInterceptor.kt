package im.threads.business.rest.config

import android.net.Uri
import im.threads.business.config.BaseConfig
import okhttp3.Interceptor
import okhttp3.Response

class BaseUrlSelectionInterceptor(private val isDatastoreApi: Boolean) : Interceptor {
    private val config = BaseConfig.instance

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val url: String = if (isDatastoreApi) config.datastoreUrl else config.serverBaseUrl
        val uri = Uri.parse(url)
        val host = uri.host
        val scheme = uri.scheme
        val path = uri.path?.let { pathDraft ->
            if (pathDraft.isNotEmpty() && pathDraft[0] == '/') {
                pathDraft.replaceFirst("/", "")
            } else {
                pathDraft
            }
        }

        val newUrl = if (host != null && scheme != null) {
            val builder = request.url.newBuilder()
                .scheme(scheme)
                .host(host)
            if (!path.isNullOrBlank()) builder.addPathSegment(path)
            builder.build()
        } else {
            null
        }

        if (newUrl != null) {
            request = request.newBuilder()
                .url(newUrl)
                .build()
        }

        return chain.proceed(request)
    }
}
