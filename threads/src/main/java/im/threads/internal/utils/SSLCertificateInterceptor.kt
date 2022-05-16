package im.threads.internal.utils

import android.util.Log
import okhttp3.Handshake
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import java.security.cert.Certificate

const val TAG = "SSL"

class SSLCertificateInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        try {
            val response: Response = chain.proceed(request)
            val handshake: Handshake? = response.handshake
            if (handshake == null) {
                Log.i(TAG, "no handshake")
                return response
            }
            Log.i(TAG, "handshake success")
            val certificates: List<Certificate> = handshake.peerCertificates
            if (certificates == null) {
                Log.i(TAG, "no peer certificates")
                return response
            }

            for (certificate in certificates) {
                Log.i(TAG, "Server $certificate")
            }
            return response
        } catch (e: Exception) {
            Log.e(TAG, "<-- HTTP FAILED: $e")
            throw e
        }
    }
}