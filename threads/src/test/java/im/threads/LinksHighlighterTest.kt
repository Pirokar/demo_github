package im.threads

import android.content.Context
import android.text.Spanned
import android.widget.TextView
import androidx.core.text.toHtml
import androidx.test.core.app.ApplicationProvider
import im.threads.business.markdown.LinkifyLinksHighlighter
import im.threads.business.markdown.LinksHighlighter
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
        val expectedHtmlResult = "<a href=\"tel:+79057755666\">+7 (905) 775-56-66</a>"

        doTest(sourceText, expectedHtmlResult)
    }

    @Test
    fun givenWithUnderline_whenMessageWithPhone_thenParsedCorrectly() {
        val sourceText = "+7.(905).775.56.66"
        val expectedHtmlResult = "<a href=\"tel:+79057755666\">+7.(905).775.56.66</a>"

        doTest(sourceText, expectedHtmlResult)
    }

    @Test
    fun givenNoUnderline_whenMessageWithEmail_thenParsedCorrectly() {
        val sourceText = "edna@gmail.com"
        val expectedHtmlResult = "<a href=\"mailto:edna@gmail.com\">"

        doTest(sourceText, expectedHtmlResult)
    }

    @Test
    fun givenNoUnderline_whenMessageWithHttpsLink_thenParsedCorrectly() {
        val sourceText = "https://edna.ru/"
        val expectedHtmlResult = "<a href=\"https://edna.ru/\">https://edna.ru/</a>"

        doTest(sourceText, expectedHtmlResult)
    }

    @Test
    fun givenNoUnderline_whenMessageWithWwwLink_thenParsedCorrectly() {
        val sourceText = "www.edna.ru"
        val expectedHtmlResult = "<a href=\"http://www.edna.ru\"><a href=\"www.edna.ru\">www.edna.ru</a></a>"

        doTest(sourceText, expectedHtmlResult)
    }

    private fun doTest(sourceText: String, expectedHtmlResult: String) {
        testTextView.text = sourceText
        linksHighlighter.highlightAllTypeOfLinks(testTextView, null, false)
        val resultHtml = (testTextView.text as Spanned).toHtml()
        assert(resultHtml.contains(expectedHtmlResult))
    }
}
