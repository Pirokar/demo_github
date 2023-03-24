package im.threads.business.transport

import im.threads.business.AuthMethod
import im.threads.business.UserInfoBuilder
import okhttp3.Request
import java.net.HttpURLConnection

class AuthHeadersProvider {
    fun getRequestWithHeaders(userInfo: UserInfoBuilder?, request: Request): Request {
        val cookiesString = getCookiesString(userInfo)

        return if (cookiesString.isNotEmpty()) {
            request
                .newBuilder()
                .addHeader(cookieHeaderKey, cookiesString)
                .build()
        } else {
            val authToken = userInfo?.authToken
            val authSchema = userInfo?.authSchema
            val requestBuilder = request.newBuilder()

            if (authToken.isNullOrEmpty()) {
                return request
            }

            requestBuilder.addHeader(authorizationHeaderKey, authToken)

            if (!authSchema.isNullOrEmpty()) {
                requestBuilder.addHeader(authSchemaHeaderKey, authSchema)
            }

            requestBuilder.build()
        }
    }

    fun setHeadersToUrlConnection(userInfo: UserInfoBuilder?, urlConnection: HttpURLConnection) {
        val cookiesString = getCookiesString(userInfo)

        if (cookiesString.isNotEmpty()) {
            urlConnection.setRequestProperty(cookieHeaderKey, cookiesString)
        } else {
            val authToken = userInfo?.authToken
            val authSchema = userInfo?.authSchema

            if (authToken.isNullOrEmpty()) {
                return
            }

            urlConnection.setRequestProperty(authorizationHeaderKey, authToken)

            if (!authSchema.isNullOrEmpty()) {
                urlConnection.setRequestProperty(authSchemaHeaderKey, authSchema)
            }
        }
    }

    private fun getCookiesString(userInfo: UserInfoBuilder?): String {
        val stringBuilder = StringBuilder()
        if (userInfo?.authMethod == AuthMethod.COOKIES) {
            val authToken = userInfo.authToken
            val authSchema = userInfo.authSchema

            stringBuilder.append("$authorizationHeaderKey=$authToken")

            if (!authSchema.isNullOrEmpty()) {
                stringBuilder.append("; $authSchemaHeaderKey=$authSchema")
            }
        }

        return stringBuilder.toString()
    }

    companion object {
        private const val cookieHeaderKey = "Cookie"
        private const val authorizationHeaderKey = "Authorization"
        private const val authSchemaHeaderKey = "X-Auth-Schema"
    }
}
