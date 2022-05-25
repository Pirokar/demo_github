package im.threads.internal.model

import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * Модель SslSocketFactory для настройки OkHttpClient.
 */
data class SslSocketFactoryConfig(
    val sslSocketFactory: SSLSocketFactory,
    val trustManager: X509TrustManager
)
