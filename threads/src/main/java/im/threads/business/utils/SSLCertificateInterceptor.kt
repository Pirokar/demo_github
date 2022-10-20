package im.threads.business.utils

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
            logCertificates(certificates, false)
            return response
        } catch (e: Exception) {
            LoggerEdna.error("<-- HTTP FAILED: $e")
            logCertificates(certificates, true)
            Thread.sleep(300)
            throw e
        }
    }

    private fun logCertificates(certificates: List<Certificate>, isError: Boolean) {
        if (certificates.isEmpty()) {
            if (isError) {
                LoggerEdna.error("no peer certificates")
            } else {
                LoggerEdna.info("no peer certificates")
            }
        } else {
            certificates.forEach {
                if (isError) {
                    LoggerEdna.error("Server $it")
                } else {
                    LoggerEdna.info("Server $it")
                }
            }
        }
    }
}
