package im.threads.internal.utils

import android.content.Context
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import im.threads.config.HttpClientSettings
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object PicassoUtils {

    @JvmStatic
    fun getOkHttpClient(httpClientSettings: HttpClientSettings): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val newRequest: Request = chain.request().newBuilder()
                    .addHeader("X-Ext-Client-ID", PrefUtils.getClientID())
                    .build()
                chain.proceed(newRequest)
            }
            .connectTimeout(httpClientSettings.connectTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(httpClientSettings.readTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(httpClientSettings.writeTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
            .build()

    @JvmStatic
    fun setPicasso(context: Context, httpClientSettings: HttpClientSettings) {
        Picasso.setSingletonInstance(
            Picasso.Builder(context)
                .downloader(OkHttp3Downloader(getOkHttpClient(httpClientSettings)))
                .build()
        )
    }
}
