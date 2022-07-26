package im.threads.internal.utils

import im.threads.internal.domain.logger.LoggerEdna
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
                LoggerEdna.i("no handshake")
                return response
            }
            LoggerEdna.i("handshake success")
            val certificates: List<Certificate> = handshake.peerCertificates
            if (certificates.isEmpty()) {
                LoggerEdna.i("no peer certificates")
            } else {
                certificates.forEach { LoggerEdna.i("Server $it") }
            }
            return response
        } catch (e: Exception) {
            LoggerEdna.e("<-- HTTP FAILED: $e")
            throw e
        }
    }
}
