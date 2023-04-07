package im.threads.business.imageLoading

import im.threads.business.UserInfoBuilder
import im.threads.business.models.SslSocketFactoryConfig
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.rest.config.HttpClientSettings
import im.threads.business.transport.AuthHeadersProvider
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSession

class ImageLoaderOkHttpProvider(
    private val preferences: Preferences,
    private val authHeadersProvider: AuthHeadersProvider
) {
    fun createOkHttpClient(
        httpClientSettings: HttpClientSettings,
        sslSocketFactoryConfig: SslSocketFactoryConfig?
    ) {
        val httpClientBuilder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val userInfo = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)
                val builder = chain.request().newBuilder().apply {
                    userInfo?.clientId?.let { addHeader("X-Ext-Client-ID", it) }
                }
                val request = authHeadersProvider.getRequestWithHeaders(userInfo, builder.build())
                chain.proceed(request)
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
