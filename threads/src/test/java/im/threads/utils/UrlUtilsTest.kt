package im.threads.utils

import im.threads.business.utils.UrlUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UrlUtilsTest {
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
