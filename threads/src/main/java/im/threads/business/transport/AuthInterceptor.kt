package im.threads.business.transport

import im.threads.business.UserInfoBuilder
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(
    private val preferences: Preferences,
    private val authHeadersProvider: AuthHeadersProvider
) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val userInfo = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)
        val request = authHeadersProvider.getRequestWithHeaders(userInfo, chain.request())
        return chain.proceed(request)
    }
}
