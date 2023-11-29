package im.threads.business.imageLoading

import im.threads.business.models.SslSocketFactoryConfig
import im.threads.business.rest.config.HttpClientSettings
import im.threads.business.transport.AuthHeadersProvider
import im.threads.business.utils.ClientUseCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ImageLoaderOkHttpProviderTest {

    private val authHeadersProvider: AuthHeadersProvider = mock(AuthHeadersProvider::class.java)
    private val clientUseCase: ClientUseCase = mock(ClientUseCase::class.java)
    private val imageLoaderOkHttpProvider = ImageLoaderOkHttpProvider(authHeadersProvider, clientUseCase)

    @Test
    fun whenCreatingOkHttpClient_thenItsSettingsEqualsToInitial() {
        val httpClientSettings = HttpClientSettings(1000, 1000, 1000)
        val sslSocketFactoryConfig: SslSocketFactoryConfig? = null

        imageLoaderOkHttpProvider.createOkHttpClient(httpClientSettings, sslSocketFactoryConfig)

        val okHttpClient = ImageLoaderOkHttpProvider.okHttpClient

        assertNotNull(okHttpClient)
        assertEquals(httpClientSettings.connectTimeoutMillis, okHttpClient?.connectTimeoutMillis?.toLong())
        assertEquals(httpClientSettings.readTimeoutMillis, okHttpClient?.readTimeoutMillis?.toLong())
        assertEquals(httpClientSettings.writeTimeoutMillis, okHttpClient?.writeTimeoutMillis?.toLong())
    }
}
