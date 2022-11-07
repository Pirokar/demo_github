package im.threads.business.config

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import com.google.gson.GsonBuilder
import im.threads.business.core.UnreadMessagesCountListener
import im.threads.business.exceptions.MetaConfigurationException
import im.threads.business.imageLoading.ImageLoaderOkHttpProvider
import im.threads.business.logger.LoggerConfig
import im.threads.business.models.SslSocketFactoryConfig
import im.threads.business.preferences.Preferences
import im.threads.business.rest.config.RequestConfig
import im.threads.business.rest.config.SocketClientSettings
import im.threads.business.serviceLocator.core.inject
import im.threads.business.transport.Transport
import im.threads.business.transport.threadsGate.ThreadsGateTransport
import im.threads.business.utils.MetadataBusiness.getDatastoreUrl
import im.threads.business.utils.MetadataBusiness.getNewChatCenterApi
import im.threads.business.utils.MetadataBusiness.getServerBaseUrl
import im.threads.business.utils.MetadataBusiness.getThreadsGateProviderUid
import im.threads.business.utils.MetadataBusiness.getThreadsGateUrl
import im.threads.business.utils.createTlsPinningKeyStore
import im.threads.business.utils.createTlsPinningSocketFactory
import im.threads.business.utils.getTrustManagers
import im.threads.business.utils.getX509TrustManager
import im.threads.business.utils.gson.UriDeserializer
import im.threads.business.utils.gson.UriSerializer
import okhttp3.Interceptor
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

open class BaseConfig(
    context: Context,
    serverBaseUrl: String?,
    datastoreUrl: String?,
    threadsGateUrl: String?,
    threadsGateProviderUid: String?,
    isNewChatCenterApi: Boolean?,
    val loggerConfig: LoggerConfig?,
    val unreadMessagesCountListener: UnreadMessagesCountListener?,
    val networkInterceptor: Interceptor?,
    val isDebugLoggingEnabled: Boolean,
    val historyLoadingCount: Int,
    val surveyCompletionDelay: Int,
    val requestConfig: RequestConfig,
    val isSSLPinningDisabled: Boolean,
    certificateRawResIds: List<Int>?
) {
    @JvmField val context: Context
    val sslSocketFactoryConfig: SslSocketFactoryConfig?

    @JvmField
    val transport: Transport
    val serverBaseUrl: String
    val datastoreUrl: String

    /**
     * set history loading count
     */
    val newChatCenterApi: Boolean

    @JvmField
    val gson = GsonBuilder()
        .registerTypeAdapter(Uri::class.java, UriSerializer())
        .registerTypeAdapter(Uri::class.java, UriDeserializer())
        .create()

    private val preferences: Preferences by inject()

    init {
        this.context = context.applicationContext
        newChatCenterApi = getIsNewChatCenterApi(isNewChatCenterApi)
        sslSocketFactoryConfig = getSslSocketFactoryConfig(certificateRawResIds)
        transport = getTransport(threadsGateUrl, threadsGateProviderUid, requestConfig.socketClientSettings)
        this.serverBaseUrl = getServerBaseUrl(serverBaseUrl)
        this.datastoreUrl = getDatastoreUrl(datastoreUrl)
        ImageLoaderOkHttpProvider(Preferences(context)).createOkHttpClient( // TODO: rewrite with SL and Kotlin
            requestConfig.picassoHttpClientSettings,
            sslSocketFactoryConfig
        )
    }

    private fun getSslSocketFactoryConfig(certificateRawResIds: List<Int>?): SslSocketFactoryConfig? {
        if (certificateRawResIds == null || certificateRawResIds.isEmpty()) return null
        val keyStore = createTlsPinningKeyStore(
            context.resources,
            certificateRawResIds
        )

        @SuppressLint("CustomX509TrustManager", "TrustAllX509TrustManager")
        val trustManagers = if (isSSLPinningDisabled) {
            arrayOf<TrustManager>(object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            })
        } else {
            getTrustManagers(keyStore)
        }

        val trustManager = getX509TrustManager(trustManagers)
        val sslSocketFactory = createTlsPinningSocketFactory(trustManagers)
        return SslSocketFactoryConfig(sslSocketFactory, trustManager)
    }

    private fun getTransport(
        providedThreadsGateUrl: String?,
        providedThreadsGateProviderUid: String?,
        socketClientSettings: SocketClientSettings
    ): Transport {
        val threadsGateProviderUid = if (!providedThreadsGateProviderUid.isNullOrBlank()) {
            providedThreadsGateProviderUid
        } else {
            getThreadsGateProviderUid(context)
        }

        val threadsGateUrl = if (!providedThreadsGateUrl.isNullOrBlank()) {
            providedThreadsGateUrl
        } else {
            getThreadsGateUrl(context)
        }
        if (threadsGateUrl.isNullOrBlank()) {
            throw MetaConfigurationException("Threads gate url is not set")
        }
        if (threadsGateProviderUid.isNullOrBlank()) {
            throw MetaConfigurationException("Threads gate provider uid is not set")
        }
        return ThreadsGateTransport(
            threadsGateUrl,
            threadsGateProviderUid,
            isDebugLoggingEnabled,
            socketClientSettings,
            sslSocketFactoryConfig,
            networkInterceptor
        )
    }

    private fun getServerBaseUrl(serverBaseUrl: String?): String {
        var baseUrl = (if (serverBaseUrl.isNullOrBlank()) getServerBaseUrl(context) else serverBaseUrl)
            ?: throw MetaConfigurationException("Neither im.threads.getServerUrl meta variable, nor serverBaseUrl were provided")
        if (!baseUrl.endsWith("/")) {
            baseUrl = "$baseUrl/"
        }
        return baseUrl
    }

    private fun getDatastoreUrl(dataStoreUrl: String?): String {
        var datastoreUrl = (if (dataStoreUrl.isNullOrBlank()) getDatastoreUrl(context) else dataStoreUrl)
            ?: throw MetaConfigurationException("Neither im.threads.getDatastoreUrl meta variable, nor datastoreUrl were provided")
        if (!datastoreUrl.endsWith("/")) {
            datastoreUrl = "$datastoreUrl/"
        }
        return datastoreUrl
    }

    private fun getIsNewChatCenterApi(isNewChatCenterApi: Boolean?): Boolean {
        return isNewChatCenterApi ?: getNewChatCenterApi(context)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: BaseConfig
    }
}
