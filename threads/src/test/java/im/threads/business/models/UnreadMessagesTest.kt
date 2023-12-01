package im.threads.business.models

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UnreadMessagesTest {

    @Test
    fun whenTimeStampIsSet_thenTimeStampIsReturned() {
        val unreadMessages = UnreadMessages(1L, 5)
        Assert.assertEquals(1L, unreadMessages.timeStamp)
    }

    @Test
    fun whenCountIsSet_thenCountIsReturned() {
        val unreadMessages = UnreadMessages(1L, 5)
        Assert.assertEquals(5, unreadMessages.count)
    }

    @Test
    fun whenIsTheSameItem_thenIsTheSameItemIsReturned() {
        val unreadMessages1 = UnreadMessages(1L, 5)
        val unreadMessages2 = UnreadMessages(1L, 5)
        Assert.assertTrue(unreadMessages1.isTheSameItem(unreadMessages2))
    }

    @Test
    fun whenThreadIdIsSet_thenThreadIdIsReturned() {
        val unreadMessages = UnreadMessages(1L, 5)
        Assert.assertNull(unreadMessages.threadId)
    }
}
