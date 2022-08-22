package im.threads.internal.imageLoading

import im.threads.business.config.HttpClientSettings
import im.threads.internal.model.SslSocketFactoryConfig
import im.threads.internal.utils.PrefUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSession

object ImageLoaderOkHttpProvider {
    var okHttpClient: OkHttpClient? = null

    fun createOkHttpClient(
        httpClientSettings: HttpClientSettings,
        sslSocketFactoryConfig: SslSocketFactoryConfig?
    ) {
        val httpClientBuilder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder().apply {
                    addHeader("X-Ext-Client-ID", PrefUtils.clientID)
                    if (!PrefUtils.authToken.isNullOrBlank()) {
                        addHeader("Authorization", PrefUtils.authToken!!)
                    }
                    if (!PrefUtils.authSchema.isNullOrBlank()) {
                        addHeader("X-Auth-Schema", PrefUtils.authSchema!!)
                    }
                }
                val newRequest: Request = builder.build()
                chain.proceed(newRequest)
            }
            .connectTimeout(httpClientSettings.connectTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(httpClientSettings.readTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(httpClientSettings.writeTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)

        if (sslSocketFactoryConfig != null) {
            httpClientBuilder.sslSocketFactory(
                sslSocketFactoryConfig.sslSocketFactory,
                sslSocketFactoryConfig.trustManager
            )
            httpClientBuilder.hostnameVerifier { hostname: String, session: SSLSession -> true }
        }
        okHttpClient = httpClientBuilder.build()
    }
}
