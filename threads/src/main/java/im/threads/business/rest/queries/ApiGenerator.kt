package im.threads.business.rest.queries

import im.threads.R
import im.threads.internal.Config
import im.threads.internal.transport.AuthInterceptor
import im.threads.internal.utils.AppInfoHelper
import im.threads.internal.utils.DeviceInfoHelper
import im.threads.internal.utils.SSLCertificateInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSession

abstract class ApiGenerator protected constructor(
    private val config: Config
) {
    protected lateinit var threadsApi: ThreadsApi
    protected lateinit var apiBuild: Retrofit

    private val userAgent: String
        get() = String.format(
            config.context.resources.getString(R.string.threads_user_agent),
            DeviceInfoHelper.getOsVersion(),
            DeviceInfoHelper.getDeviceName(),
            DeviceInfoHelper.getIpAddress(),
            AppInfoHelper.getAppVersion(),
            AppInfoHelper.getAppId(),
            AppInfoHelper.getLibVersion()
        )

    init { init() }

    abstract fun createThreadsApi()

    private fun createOkHttpClient(): OkHttpClient {
        val (connectTimeoutMillis, readTimeoutMillis, writeTimeoutMillis) =
            config.requestConfig.threadsApiHttpClientSettings
        val httpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
            .addInterceptor(
                Interceptor { chain: Interceptor.Chain ->
                    chain.proceed(
                        chain.request()
                            .newBuilder()
                            .header(USER_AGENT_HEADER, userAgent)
                            .build()
                    )
                }
            )
            .addInterceptor(AuthInterceptor())
            .apply { config.networkInterceptor?.let { addInterceptor(it) } }
            .connectTimeout(connectTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(readTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(writeTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
        if (config.isDebugLoggingEnabled) {
            httpClientBuilder.addInterceptor(
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            )
            httpClientBuilder.addInterceptor(SSLCertificateInterceptor())
        }
        val sslSocketFactoryConfig = config.sslSocketFactoryConfig
        if (sslSocketFactoryConfig != null) {
            httpClientBuilder.sslSocketFactory(
                sslSocketFactoryConfig.sslSocketFactory,
                sslSocketFactoryConfig.trustManager
            )
            httpClientBuilder.hostnameVerifier { _: String?, _: SSLSession? -> true }
        }
        return httpClientBuilder.build()
    }

    private fun init() {
        apiBuild = Retrofit.Builder()
            .baseUrl(config.serverBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(createOkHttpClient())
            .build()
        createThreadsApi()
    }

    companion object {
        private const val USER_AGENT_HEADER = "User-Agent"
    }
}
