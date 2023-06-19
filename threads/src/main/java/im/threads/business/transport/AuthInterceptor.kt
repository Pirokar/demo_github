package im.threads.business.transport

import im.threads.business.preferences.Preferences
import im.threads.business.utils.ClientUseCase
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(
    private val preferences: Preferences,
    private val authHeadersProvider: AuthHeadersProvider,
    private val clientUseCase: ClientUseCase
) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val userInfo = clientUseCase.getUserInfo()
        val request = authHeadersProvider.getRequestWithHeaders(userInfo, chain.request())
        return chain.proceed(request)
    }
}
