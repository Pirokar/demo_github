package im.threads.business.imageLoading

import im.threads.business.UserInfoBuilder
import im.threads.business.models.SslSocketFactoryConfig
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.rest.config.HttpClientSettings
import im.threads.business.serviceLocator.core.get
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSession

class ImageLoaderOkHttpProvider(private val preferences: Preferences) {
    fun createOkHttpClient(
        httpClientSettings: HttpClientSettings,
        sslSocketFactoryConfig: SslSocketFactoryConfig?
    ) {
        val httpClientBuilder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder().apply {
                    val userInfo = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)
                    userInfo?.clientId?.let { addHeader("X-Ext-Client-ID", it) }
                    if (!userInfo?.authToken.isNullOrBlank()) {
                        addHeader("Authorization", userInfo?.authToken!!)
                    }
                    if (!userInfo?.authSchema.isNullOrBlank()) {
                        addHeader("X-Auth-Schema", userInfo?.authSchema!!)
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

    companion object {
        var okHttpClient: OkHttpClient? = null
    }
}
