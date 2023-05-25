@file:JvmName("TlsConfigurationUtils")

package im.threads.business.utils

import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import androidx.annotation.RawRes
import im.threads.business.logger.LoggerEdna
import java.io.InputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Методы для конфигурации SSL-пиннинга.
 */

fun getTrustManagers(keyStore: KeyStore): Array<TrustManager> {
    val defaultAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
    val trustManagerFactory = TrustManagerFactory.getInstance(defaultAlgorithm)
    trustManagerFactory.init(keyStore)
    return trustManagerFactory.trustManagers
}

fun getX509TrustManager(trustManagers: Array<TrustManager>): X509TrustManager =
    trustManagers.first { trustManager -> trustManager is X509TrustManager } as X509TrustManager

fun createTlsPinningKeyStore(
    resources: Resources,
    trustedSSLCertificates: List<Int>
): KeyStore {
    val certificateFactory = CertificateFactory.getInstance(CERTIFICATE_FORMAT)
    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
    keyStore.load(null, null)

    for (trustedSSLCertificate in trustedSSLCertificates) {
        loadCertificateIntoKeyStore(certificateFactory, keyStore, resources, trustedSSLCertificate)
    }
    return keyStore
}

private fun loadCertificateIntoKeyStore(
    certificateFactory: CertificateFactory,
    keyStore: KeyStore,
    resources: Resources,
    @RawRes rawResId: Int
) {
    var source: InputStream? = null
    try {
        source = resources.openRawResource(rawResId)
        val certificate = certificateFactory.generateCertificate(source)
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                SSLCertificateInterceptor.logCertificates(listOf(certificate), null, "Local SSL certificate")
            } catch (e: Exception) {
                LoggerEdna.error("SSLCertificatesHandling", "Impossible to get certificate", e)
            }
        }, 1000)
        val alias = resources.getResourceName(rawResId)
        keyStore.setCertificateEntry(alias, certificate)
    } finally {
        source?.close()
    }
}

fun createTlsPinningSocketFactory(trustManagers: Array<TrustManager>): SSLSocketFactory {
    val sslContext = SSLContext.getInstance(PROTOCOL_TLS).apply {
        init(null, trustManagers, SecureRandom())
    }
    return sslContext.socketFactory
}

private const val CERTIFICATE_FORMAT = "X.509"
private const val PROTOCOL_TLS = "TLS"
