package im.threads.business.utils

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UrlUtilsTest {

    @Mock
    private lateinit var intent: Intent

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun whenExtractLink_thenLinkIsExtracted() {
        val result = UrlUtils.extractLink("https://www.google.com")
        assertEquals("https://www.google.com", result?.link)
    }

    @Test
    fun whenExtractLinkWithNoLink_thenNullIsReturned() {
        val result = UrlUtils.extractLink("Hello World")
        assertNull(result)
    }

    @Test
    fun whenExtractDeepLink_thenDeepLinkIsExtracted() {
        val result = UrlUtils.extractDeepLink("myapp://page/1")
        assertEquals("myapp://page/1", result)
    }

    @Test
    fun whenExtractDeepLinkWithNoDeepLink_thenNullIsReturned() {
        val result = UrlUtils.extractDeepLink("Hello World")
        assertNull(result)
    }

    @Test
    fun whenExtractEmailAddresses_thenEmailAddressesAreExtracted() {
        val result = UrlUtils.extractEmailAddresses("Hello john.doe@example.com and jane.doe@example.com")
        assertEquals(listOf("john.doe@example.com", "jane.doe@example.com"), result)
    }

    @Test
    fun whenExtractEmailAddressesWithNoEmailAddresses_thenEmptyListIsReturned() {
        val result = UrlUtils.extractEmailAddresses("Hello World")
        assert(result.isEmpty())
    }

    @Test
    fun whenIsValidUrl_thenTrueIsReturned() {
        val result = UrlUtils.isValidUrl("https://www.google.com")
        assert(result)
    }

    @Test
    fun whenIsValidUrlWithInvalidUrl_thenFalseIsReturned() {
        val result = UrlUtils.isValidUrl("Hello World")
        assert(!result)
    }
}
