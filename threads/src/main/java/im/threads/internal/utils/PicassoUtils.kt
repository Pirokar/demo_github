package im.threads.internal.utils

import android.content.Context
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import im.threads.config.HttpClientSettings
import im.threads.internal.model.SslSocketFactoryConfig
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSession
import okhttp3.OkHttpClient
import okhttp3.Request

object PicassoUtils {

    @JvmStatic
    fun getOkHttpClient(
        httpClientSettings: HttpClientSettings,
        sslSocketFactoryConfig: SslSocketFactoryConfig?
    ): OkHttpClient {
        val httpClientBuilder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder().apply {
                    addHeader("X-Ext-Client-ID", PrefUtils.getClientID())
                    if (!PrefUtils.getAuthToken().isNullOrBlank()) {
                        addHeader("Authorization", PrefUtils.getAuthToken())
                    }
                    if (!PrefUtils.getAuthSchema().isNullOrBlank()) {
                        addHeader("X-Auth-Schema", PrefUtils.getAuthSchema())
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
        return httpClientBuilder.build()
    }

    @JvmStatic
    fun setPicasso(
        context: Context,
        httpClientSettings: HttpClientSettings,
        sslSocketFactoryConfig: SslSocketFactoryConfig?
    ) {
        val httpClient = getOkHttpClient(httpClientSettings, sslSocketFactoryConfig)
        Picasso.setSingletonInstance(
            Picasso.Builder(context)
                .downloader(OkHttp3Downloader(httpClient))
                .build()
        )
    }
}
