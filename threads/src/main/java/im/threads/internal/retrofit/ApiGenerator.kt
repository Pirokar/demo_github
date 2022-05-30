package im.threads.internal.retrofit

import im.threads.R
import im.threads.internal.Config
import im.threads.internal.transport.AuthInterceptor
import im.threads.internal.utils.AppInfoHelper
import im.threads.internal.utils.DeviceInfoHelper
import im.threads.internal.utils.SSLCertificateInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSession

class ApiGenerator private constructor() {
    private val threadsApi: ThreadsApi
    private fun createOkHttpClient(): OkHttpClient {
        val config = Config.instance
        val (connectTimeoutMillis, readTimeoutMillis, writeTimeoutMillis) =
            config.requestConfig.threadsApiHttpClientSettings
        val httpClientBuilder: Builder = Builder()
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
            .apply { Config.instance.networkInterceptor?.let { addInterceptor(it) } }
            .connectTimeout(connectTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(readTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(writeTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
        if (Config.instance.isDebugLoggingEnabled) {
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

    private val userAgent: String
        private get() = String.format(
            Config.instance.context.resources.getString(R.string.threads_user_agent),
            DeviceInfoHelper.getOsVersion(),
            DeviceInfoHelper.getDeviceName(),
            DeviceInfoHelper.getIpAddress(),
            AppInfoHelper.getAppVersion(),
            AppInfoHelper.getAppId(),
            AppInfoHelper.getLibVersion()
        )

    companion object {
        private const val USER_AGENT_HEADER = "User-Agent"
        private var apiGenerator: ApiGenerator? = null
        @JvmStatic
        fun getThreadsApi(): ThreadsApi {
            if (apiGenerator == null) {
                apiGenerator = ApiGenerator()
            }
            return apiGenerator!!.threadsApi
        }
    }

    init {
        val build = Retrofit.Builder()
            .baseUrl(Config.instance.serverBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(createOkHttpClient())
            .build()
        threadsApi = ThreadsApi(
            build.create(OldThreadsApi::class.java),
            build.create(NewThreadsApi::class.java)
        )
    }
}
