package im.threads.business.rest.queries

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import im.threads.R
import im.threads.business.config.BaseConfig
import im.threads.business.logger.NetworkLoggerInterceptor
import im.threads.business.rest.config.BaseUrlSelectionInterceptor
import im.threads.business.serviceLocator.core.inject
import im.threads.business.transport.AuthInterceptor
import im.threads.business.utils.AppInfoHelper
import im.threads.business.utils.DeviceInfoHelper
import im.threads.business.utils.SSLCertificateInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSession

abstract class ApiGenerator protected constructor(
    private val config: BaseConfig,
    private val isDatastoreApi: Boolean
) {
    protected lateinit var threadsApi: ThreadsApi
    protected lateinit var apiBuild: Retrofit
    private val authInterceptor: AuthInterceptor by inject()

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
            .addInterceptor(BaseUrlSelectionInterceptor(isDatastoreApi))
            .cache(null)
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
            .addInterceptor(authInterceptor)
            .apply { config.networkInterceptor?.let { addInterceptor(it) } }
            .connectTimeout(connectTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(readTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(writeTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
        if (config.isDebugLoggingEnabled) {
            httpClientBuilder.addInterceptor(
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            )
        }
        val sslSocketFactoryConfig = config.sslSocketFactoryConfig
        if (sslSocketFactoryConfig != null) {
            if (config.isDebugLoggingEnabled) {
                httpClientBuilder.addInterceptor(SSLCertificateInterceptor())
            }

            httpClientBuilder.sslSocketFactory(
                sslSocketFactoryConfig.sslSocketFactory,
                sslSocketFactoryConfig.trustManager
            )
            httpClientBuilder.hostnameVerifier { _: String?, _: SSLSession? -> true }
        }
        httpClientBuilder.addInterceptor(NetworkLoggerInterceptor())
        return httpClientBuilder.build()
    }

    private fun init() {
        val gson: Gson = GsonBuilder()
            .setLenient()
            .create()

        apiBuild = Retrofit.Builder()
            .baseUrl("http://nourl.com")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(createOkHttpClient())
            .build()
        createThreadsApi()
    }

    companion object {
        private const val USER_AGENT_HEADER = "User-Agent"
    }
}
