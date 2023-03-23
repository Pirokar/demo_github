package im.threads.business.transport

import im.threads.business.AuthMethod
import im.threads.business.AuthMethod.Companion.fromString
import im.threads.business.UserInfoBuilder
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(private val preferences: Preferences) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val userInfo = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)
        val authToken = userInfo?.authToken
        val authMethod = userInfo?.authMethod
        val authSchema = userInfo?.authSchema
        val request: Request = if (!authToken.isNullOrBlank() && fromString(authMethod) == AuthMethod.HEADERS) {
            chain.request()
                .newBuilder()
                .addHeader("Authorization", authToken)
                .addHeader("X-Auth-Schema", authSchema ?: "")
                .build()
        } else if (!authToken.isNullOrBlank() && fromString(authMethod) == AuthMethod.COOKIES) {
            chain.request()
                .newBuilder()
                .addHeader("Cookie", "Authorization=$authToken; X-Auth-Schema=$authSchema")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
