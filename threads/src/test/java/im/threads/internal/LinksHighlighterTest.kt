package im.threads.internal

import android.content.Context
import android.text.Spanned
import android.widget.TextView
import androidx.core.text.toHtml
import androidx.test.core.app.ApplicationProvider
import im.threads.internal.markdown.LinkifyLinksHighlighter
import im.threads.internal.markdown.LinksHighlighter
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LinksHighlighterTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var testTextView: TextView
    private lateinit var linksHighlighter: LinksHighlighter

    @Before
    fun before() {
        testTextView = TextView(context)
        linksHighlighter = LinkifyLinksHighlighter()
    }

    @Test
    fun givenNoUnderline_whenMessageWithPhone_thenParsedCorrectly() {
        val sourceText = "+7 (905) 775-56-66"
        val expectedHtmlResult = "<p dir=\"ltr\"><a href=\"tel:+79057755666\">+7 (905) 775-56-66</a></p>\n"

        doTest(sourceText, expectedHtmlResult)
    }

    @Test
    fun givenWithUnderline_whenMessageWithPhone_thenParsedCorrectly() {
        val sourceText = "+7.(905).775.56.66"
        val expectedHtmlResult = "<p dir=\"ltr\"><a href=\"tel:+79057755666\">+7.(905).775.56.66</a></p>\n"

        doTest(sourceText, expectedHtmlResult)
    }

    @Test
    fun givenNoUnderline_whenMessageWithEmail_thenParsedCorrectly() {
        val sourceText = "edna@gmail.com"
        val expectedHtmlResult = "<p dir=\"ltr\"><a href=\"mailto:edna@gmail.com\">edna@gmail.com</a></p>\n"

        doTest(sourceText, expectedHtmlResult)
    }

    @Test
    fun givenNoUnderline_whenMessageWithHttpsLink_thenParsedCorrectly() {
        val sourceText = "https://edna.ru/"
        val expectedHtmlResult = "<p dir=\"ltr\"><a href=\"https://edna.ru/\">https://edna.ru/</a></p>\n"

        doTest(sourceText, expectedHtmlResult)
    }

    @Test
    fun givenNoUnderline_whenMessageWithWwwLink_thenParsedCorrectly() {
        val sourceText = "www.edna.ru"
        val expectedHtmlResult = "<p dir=\"ltr\"><a href=\"http://www.edna.ru\">www.edna.ru</a></p>\n"

        doTest(sourceText, expectedHtmlResult)
    }

    private fun doTest(sourceText: String, expectedHtmlResult: String) {
        testTextView.text = sourceText
        linksHighlighter.highlightAllTypeOfLinks(testTextView, false)
        val resultHtml = (testTextView.text as Spanned).toHtml()
        assert(resultHtml == expectedHtmlResult)
    }
}
