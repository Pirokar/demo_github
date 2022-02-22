package im.threads.internal.utils

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object PicassoUtils {

    @JvmStatic
    fun getOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                val newRequest: Request = chain.request().newBuilder()
                    .addHeader("X-Ext-Client-ID", PrefUtils.getClientID())
                    .build()
                return chain.proceed(newRequest)
            }
        })
        .build()

    @JvmStatic
    fun setPicasso(context: Context)  {
        Picasso.setSingletonInstance(Picasso.Builder(context)
            .downloader(OkHttp3Downloader(getOkHttpClient()))
            .build())
    }
}
