package im.threads.business.transport

import im.threads.business.AuthMethod
import im.threads.business.UserInfoBuilder
import okhttp3.Request
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.URL
import javax.net.ssl.HttpsURLConnection

@RunWith(RobolectricTestRunner::class)
class AuthHeadersProviderTest {
    private val authHeadersProvider = AuthHeadersProvider()
    private val testUrl = "https://edna.ru"
    private val testRequest = Request
        .Builder()
        .url(testUrl)
        .build()
    private val testClientId = "test12345"
    private val testToken = "sdsdfsdfewrft2353"
    private val testSchema = "retail"
    private val cookieHeaderKey = "Cookie"
    private val authorizationHeaderKey = "Authorization"
    private val authSchemaHeaderKey = "X-Auth-Schema"

    @Test
    fun givenAuthMethodIsHeaderWithRetrofit_whenGetRequestWithHeaders_thenHeadersInRequest() {
        val userInfo = UserInfoBuilder(testClientId).apply {
            setAuthData(testToken, testSchema, AuthMethod.HEADERS)
        }
        testFullHeadersWithRetrofit(userInfo)
    }

    @Test
    fun givenNoAuthMethodWithRetrofit_whenGetRequestWithHeaders_thenHeadersInRequest() {
        val userInfo = UserInfoBuilder(testClientId).apply {
            setAuthData(testToken, testSchema)
        }
        testFullHeadersWithRetrofit(userInfo)
    }

    @Test
    fun givenAuthMethodIsHeaderAndNoSchemeWithRetrofit_whenGetRequestWithHeaders_thenAuthOnlyInRequest() {
        val userInfo = UserInfoBuilder(testClientId).apply {
            setAuthData(testToken, null, AuthMethod.HEADERS)
        }
        val request = authHeadersProvider.getRequestWithHeaders(userInfo, testRequest)
        assert(
            request.headers.names().size == 1 &&
                request.headers.values(authorizationHeaderKey)[0] == testToken
        )
    }

    @Test
    fun givenAuthMethodIsCookiesWithRetrofit_whenGetRequestWithHeaders_thenCookiesInRequest() {
        val userInfo = UserInfoBuilder(testClientId).apply {
            setAuthData(testToken, testSchema, AuthMethod.COOKIES)
        }
        val request = authHeadersProvider.getRequestWithHeaders(userInfo, testRequest)
        assert(
            request.headers.values(cookieHeaderKey)[0] == "$authorizationHeaderKey=$testToken; $authSchemaHeaderKey=$testSchema"
        )
    }

    @Test
    fun givenAuthMethodIsCookiesAndNoSchemeWithRetrofit_whenGetRequestWithHeaders_thenAuthOnlyInRequest() {
        val userInfo = UserInfoBuilder(testClientId).apply {
            setAuthData(testToken, null, AuthMethod.COOKIES)
        }
        val request = authHeadersProvider.getRequestWithHeaders(userInfo, testRequest)
        assert(
            request.headers.values(cookieHeaderKey)[0] == "$authorizationHeaderKey=$testToken"
        )
    }

    @Test
    fun givenAuthMethodIsHeaderWithUrlConnection_whenSetHeadersToUrlConnection_thenHeadersInUrlConnection() {
        val userInfo = UserInfoBuilder(testClientId).apply {
            setAuthData(testToken, testSchema, AuthMethod.HEADERS)
        }
        testFullHeadersWithUrlConnection(userInfo)
    }

    @Test
    fun givenNoAuthMethodIsHeaderWithUrlConnection_whenSetHeadersToUrlConnection_thenHeadersInUrlConnection() {
        val userInfo = UserInfoBuilder(testClientId).apply {
            setAuthData(testToken, testSchema)
        }
        testFullHeadersWithUrlConnection(userInfo)
    }

    @Test
    fun givenAuthMethodIsHeaderAndNoSchemeWithUrlConnection_whenSetHeadersToUrlConnection_thenNoSchemeInConnection() {
        val userInfo = UserInfoBuilder(testClientId).apply {
            setAuthData(testToken, null, AuthMethod.HEADERS)
        }
        val urlConnection = getTestUrlConnection()
        authHeadersProvider.setHeadersToUrlConnection(userInfo, urlConnection)
        assert(
            urlConnection.getRequestProperty(authSchemaHeaderKey) == null
        )
    }

    @Test
    fun givenAuthMethodIsCookiesWithUrlConnection_whenSetHeadersToUrlConnection_thenCookiesInConnection() {
        val userInfo = UserInfoBuilder(testClientId).apply {
            setAuthData(testToken, testSchema, AuthMethod.COOKIES)
        }
        val urlConnection = getTestUrlConnection()
        authHeadersProvider.setHeadersToUrlConnection(userInfo, urlConnection)
        assert(
            urlConnection.getRequestProperty(cookieHeaderKey) == "$authorizationHeaderKey=$testToken; $authSchemaHeaderKey=$testSchema"
        )
    }

    @Test
    fun givenAuthMethodIsCookiesAndNoSchemeWithUrlConnection_whenSetHeadersToUrlConnection_thenAuthOnlyInConnection() {
        val userInfo = UserInfoBuilder(testClientId).apply {
            setAuthData(testToken, null, AuthMethod.COOKIES)
        }
        val urlConnection = getTestUrlConnection()
        authHeadersProvider.setHeadersToUrlConnection(userInfo, urlConnection)
        assert(
            urlConnection.getRequestProperty(cookieHeaderKey) == "$authorizationHeaderKey=$testToken"
        )
    }

    private fun testFullHeadersWithRetrofit(userInfo: UserInfoBuilder) {
        val request = authHeadersProvider.getRequestWithHeaders(userInfo, testRequest)
        assert(
            request.headers.values(authorizationHeaderKey)[0] == testToken &&
                request.headers.values(authSchemaHeaderKey)[0] == testSchema
        )
    }

    private fun testFullHeadersWithUrlConnection(userInfo: UserInfoBuilder) {
        val urlConnection = getTestUrlConnection()
        authHeadersProvider.setHeadersToUrlConnection(userInfo, urlConnection)
        assert(
            urlConnection.getRequestProperty(authSchemaHeaderKey) == testSchema
        )
    }

    private fun getTestUrlConnection(): HttpsURLConnection {
        return (URL(testUrl).openConnection() as HttpsURLConnection).apply {
            requestMethod = "GET"
        }
    }
}
