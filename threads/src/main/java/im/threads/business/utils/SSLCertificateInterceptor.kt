package im.threads.business.utils

import im.threads.business.extensions.fullLogString
import im.threads.business.logger.LoggerEdna
import okhttp3.Handshake
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import java.security.cert.Certificate

class SSLCertificateInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        var certificates: List<Certificate> = listOf()
        try {
            val response: Response = chain.proceed(request)
            val handshake: Handshake? = response.handshake
            if (handshake == null) {
                LoggerEdna.info(tag, "no handshake")
                return response
            }
            LoggerEdna.info(tag, "handshake success")
            certificates = handshake.peerCertificates
            logCertificates(certificates, null, "Peer (remote) certificates")
            return response
        } catch (e: Exception) {
            logCertificates(certificates, e, "Peer (remote) certificates")
            Thread.sleep(300)
            throw e
        }
    }

    companion object {
        private const val tag = "SSLCertificatesHandling"
        private var alreadyPrintedCertificates = mutableListOf<String>()

        fun logCertificates(certificates: List<Certificate>, error: Exception?, certificateName: String) {
            if (certificates.isEmpty()) {
                LoggerEdna.error(tag, "No $certificateName, list is empty")
            } else if (error != null) {
                LoggerEdna.error(tag, error.fullLogString())
            } else {
                val message = StringBuilder().apply {
                    append("Available $certificateName:\n")
                }
                certificates.forEach {
                    message.append(it)
                }
                val messageString = message.toString()
                if (!alreadyPrintedCertificates.contains(messageString)) {
                    LoggerEdna.info(tag, messageString)
                    alreadyPrintedCertificates.add(messageString)
                }
            }
        }
    }
}
