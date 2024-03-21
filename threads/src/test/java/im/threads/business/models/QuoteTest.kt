package im.threads.business.models

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class QuoteTest {

    @Test
    fun whenEquals_thenCorrect() {
        val quote1 = Quote("uuid", "title", "text", null, 0L)
        val quote2 = Quote("uuid", "title", "text", null, 0L)
        Assert.assertTrue(quote1 == quote2)

        val quote3 = Quote("uuid", "title", "different text", null, 0L)
        Assert.assertFalse(quote1 == quote3)
    }

    @Test
    fun whenHashCode_thenCorrect() {
        val quote = Quote("uuid", "title", "text", null, 0L)
        val expectedHashCode = 31 * ("text".hashCode()) + 0
        Assert.assertEquals(expectedHashCode, quote.hashCode())
    }

    @Test
    fun whenToString_thenCorrect() {
        val quote = Quote("uuid", "title", "text", null, 0L)
        val expectedString = "Quote{phraseOwnerTitle='title', text='text', fileDescription=null, timeStamp=0, isFromConsult=false, " +
            "modified=NONE, isPersonalOffer=false, quotedPhraseConsultId='null'}"
        Assert.assertEquals(expectedString, quote.toString())
    }

    @Test
    fun whenHasSameContent_thenCorrect() {
        val quote1 = Quote("uuid", "title", "text", null, 0L)
        val quote2 = Quote("uuid", "title", "text", null, 0L)
        Assert.assertTrue(quote1.hasSameContent(quote2))

        val quote3 = Quote("uuid", "title", "different text", null, 0L)
        Assert.assertFalse(quote1.hasSameContent(quote3))
    }
}
