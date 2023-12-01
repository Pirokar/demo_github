package im.threads.business.models

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NoChatItemTest {

    @Test
    fun whenTimeStampIsZero_thenTimeStampIsZero() {
        val noChatItem = NoChatItem()
        Assert.assertEquals(0L, noChatItem.timeStamp)
    }

    @Test
    fun whenThreadIdIsNull_thenThreadIdIsZero() {
        val noChatItem = NoChatItem()
        assert(noChatItem.threadId == 0L)
    }

    @Test
    fun whenIsTheSameItem_thenAlwaysFalse() {
        val noChatItem = NoChatItem()
        Assert.assertFalse(noChatItem.isTheSameItem(null))
    }
}
