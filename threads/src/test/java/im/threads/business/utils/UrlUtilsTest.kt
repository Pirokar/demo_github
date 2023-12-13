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

    @Test
    fun givenHttpsLinkWithSlash_whenExtractingLink_thenExtractedLinkCorrect() {
        assert(isLinkOk("https://edna.ru/"))
    }

    @Test
    fun givenHttpsLinkNoSlash_whenExtractingLink_thenExtractedLinkCorrect() {
        assert(isLinkOk("https://edna.ru"))
    }

    @Test
    fun givenWebViewLinkWithSlash_whenExtractingLink_thenExtractedLinkCorrect() {
        assert(isLinkOk("webview://edna.ru/"))
    }

    @Test
    fun givenWebViewLinkNoSlash_whenExtractingLink_thenExtractedLinkCorrect() {
        assert(isLinkOk("webview://edna.ru"))
    }

    @Test
    fun givenWrongLink_whenExtractingLink_thenExtractedLinkIncorrect() {
        assert(!isLinkOk("webview://34df**"))
    }

    @Test
    fun givenEmail_whenExtractingLink_thenExtractedLinkCorrect() {
        val text = "Hello! This is Edna. Our email is edna@edna.ru, please visit us!"
        val extractedLink = UrlUtils.extractLink(text)

        assert(extractedLink != null && extractedLink.isEmail && extractedLink.link == "edna.ru")
    }

    @Test
    fun givenDeeplink_whenExtractingLink_thenExtractedLinkCorrect() {
        val link = "play://somelink"
        val text = "Hello! This is Edna. Our link is $link"
        val extractedLink = UrlUtils.extractDeepLink(text)

        assert(extractedLink == link)
    }

    @Test
    fun givenImagesWithAllExtensions_whenExtractingImageUrl_thenLinkIsCorrect() {
        val baseLink = "https://edna.ru/image"
        var result = true
        for (extension in UrlUtils.imageExtensions) {
            val link = "$baseLink$extension"
            val text = "Hello this is me when I was a child: $link. Am I good?"
            if (UrlUtils.extractImageMarkdownLink(text) != link) {
                result = false
                break
            }
        }
        assert(result)
    }

    private fun isLinkOk(link: String): Boolean {
        val text = "Hello! This is Edna. Our link is $link, please visit us!"
        val extractedLink = UrlUtils.extractLink(text)

        return extractedLink != null && !extractedLink.isEmail && extractedLink.link == link
    }
}
