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
                LoggerEdna.info("no handshake")
                return response
            }
            LoggerEdna.info("handshake success")
            certificates = handshake.peerCertificates
            logCertificates(certificates, null)
            return response
        } catch (e: Exception) {
            logCertificates(certificates, e)
            Thread.sleep(300)
            throw e
        }
    }

    private fun logCertificates(certificates: List<Certificate>, error: Exception?) {
        if (certificates.isEmpty()) {
            LoggerEdna.error("no peer certificates, list is empty")
        } else if (error != null) {
            LoggerEdna.error(error.fullLogString())
        } else {
            val message = StringBuilder().apply {
                append("Available certificates:\n")
            }
            certificates.forEach {
                message.append(it)
            }
            LoggerEdna.info(message.toString())
        }
    }
}
