package im.threads.business.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StringExtensionsTest {

    @Test
    fun whenPaddingStart_thenPaddingIsAdded() {
        val result = "Hello".paddingStart(3, "-")
        assertEquals("---Hello", result)
    }

    @Test
    fun whenPaddingStartWithNoDelimiter_thenSpacesAreAdded() {
        val result = "Hello".paddingStart(3)
        assertEquals("   Hello", result)
    }

    @Test
    fun whenPaddingEnd_thenPaddingIsAdded() {
        val result = "Hello".paddingEnd(3, "-")
        assertEquals("Hello---", result)
    }

    @Test
    fun whenPaddingEndWithNoDelimiter_thenSpacesAreAdded() {
        val result = "Hello".paddingEnd(3)
        assertEquals("Hello   ", result)
    }

    @Test
    fun whenPaddingStartEnd_thenPaddingIsAdded() {
        val result = "Hello".paddingStartEnd(3, 3, "-", "-")
        assertEquals("---Hello---", result)
    }

    @Test
    fun whenPaddingStartEndWithNoDelimiter_thenSpacesAreAdded() {
        val result = "Hello".paddingStartEnd(3, 3)
        assertEquals("   Hello   ", result)
    }

    @Test
    fun whenEncodeUrl_thenUrlIsEncoded() {
        val result = "Hello World!".encodeUrl()
        assertEquals("Hello%20World!", result)
    }

    @Test
    fun whenEncodeUrlWithSpecialCharacters_thenUrlIsEncoded() {
        val result = "Hello@World!".encodeUrl()
        assertEquals("Hello@World!", result)
    }

    @Test
    fun whenHasSubstrings_thenTrueIsReturned() {
        val result = "Hello World!".hasSubstrings(listOf("Hello", "World"))
        assertEquals(true, result)
    }

    @Test
    fun whenHasSubstringsWithNoMatch_thenFalseIsReturned() {
        val result = "Hello World!".hasSubstrings(listOf("Goodbye", "Universe"))
        assertEquals(false, result)
    }
}
