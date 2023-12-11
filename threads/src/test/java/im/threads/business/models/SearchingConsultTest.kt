package im.threads.business.models

import androidx.core.util.ObjectsCompat
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SearchingConsultTest {

    @Test
    fun whenIsTheSameItem_thenCorrect() {
        val searchingConsult1 = SearchingConsult()
        val searchingConsult2 = SearchingConsult()
        Assert.assertTrue(searchingConsult1.isTheSameItem(searchingConsult2))

        val chatItem = object : ChatItem {
            override val timeStamp: Long = 0L
            override fun isTheSameItem(otherItem: ChatItem?): Boolean = false
            override val threadId: Long? = null
        }
        Assert.assertFalse(searchingConsult1.isTheSameItem(chatItem))
    }

    @Test
    fun whenEquals_thenCorrect() {
        val searchingConsult1 = SearchingConsult()
        val searchingConsult2 = SearchingConsult()
        searchingConsult2.setDate(1L)
        Assert.assertFalse(searchingConsult1 == searchingConsult2)
    }

    @Test
    fun whenHashCode_thenCorrect() {
        val searchingConsult = SearchingConsult()
        val expectedHashCode = ObjectsCompat.hash(searchingConsult.timeStamp)
        Assert.assertEquals(expectedHashCode, searchingConsult.hashCode())
    }

    @Test
    fun whenToString_thenCorrect() {
        val searchingConsult = SearchingConsult()
        val expectedString = "SearchingConsult{date=" + searchingConsult.timeStamp + '}'
        Assert.assertEquals(expectedString, searchingConsult.toString())
    }
}
