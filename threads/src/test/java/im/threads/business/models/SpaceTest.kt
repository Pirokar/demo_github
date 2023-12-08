package im.threads.business.models

import androidx.core.util.ObjectsCompat
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SpaceTest {

    @Test
    fun whenIsTheSameItem_thenCorrect() {
        val space1 = Space(10, 0L)
        val space2 = Space(10, 0L)
        Assert.assertTrue(space1.isTheSameItem(space2))

        val chatItem = object : ChatItem {
            override val timeStamp: Long = 0L
            override fun isTheSameItem(otherItem: ChatItem?): Boolean = false
            override val threadId: Long? = null
        }
        Assert.assertFalse(space1.isTheSameItem(chatItem))
    }

    @Test
    fun whenEquals_thenCorrect() {
        val space1 = Space(10, 0L)
        val space2 = Space(10, 0L)
        Assert.assertTrue(space1 == space2)

        val space3 = Space(20, 0L)
        Assert.assertFalse(space1 == space3)
    }

    @Test
    fun whenHashCode_thenCorrect() {
        val space = Space(10, 0L)
        val expectedHashCode = ObjectsCompat.hash(10, 0L)
        Assert.assertEquals(expectedHashCode, space.hashCode())
    }

    @Test
    fun whenToString_thenCorrect() {
        val space = Space(10, 0L)
        val expectedString = "Space{height=10, timeStamp=0}"
        Assert.assertEquals(expectedString, space.toString())
    }
}
