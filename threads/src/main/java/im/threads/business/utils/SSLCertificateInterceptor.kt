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
        try {
            val response: Response = chain.proceed(request)
            val handshake: Handshake? = response.handshake
            if (handshake == null) {
                LoggerEdna.info("no handshake")
                return response
            }
            LoggerEdna.info("handshake success")
            val certificates: List<Certificate> = handshake.peerCertificates
            if (certificates.isEmpty()) {
                LoggerEdna.info("no peer certificates")
            } else {
                certificates.forEach { LoggerEdna.info("Server $it") }
            }
            return response
        } catch (e: Exception) {
            LoggerEdna.error("<-- HTTP FAILED: $e")
            throw e
        }
    }
}
