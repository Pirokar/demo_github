package im.threads.business.models

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GetStatusesActionTest {

    @Test
    fun whenNewInstance_thenPropertiesAreSet() {
        val messageId = listOf("id1", "id2")
        val action = GetStatusesAction(messageId)

        Assert.assertEquals(messageId, action.messageId)
        Assert.assertEquals(0, action.timeStamp)
        Assert.assertNull(action.threadId)
    }

    @Test
    fun whenSameInstance_thenIsTheSameItemReturnsTrue() {
        val action = GetStatusesAction(listOf("id1", "id2"))

        Assert.assertTrue(action.isTheSameItem(action))
    }

    @Test
    fun whenDifferentType_thenIsTheSameItemReturnsFalse() {
        val action = GetStatusesAction(listOf("id1", "id2"))
        val otherItem: ChatItem = object : ChatItem {
            override var timeStamp: Long = 0
            override var threadId: Long? = null
            override fun isTheSameItem(otherItem: ChatItem?): Boolean = false
        }

        Assert.assertFalse(action.isTheSameItem(otherItem))
    }

    @Test
    fun whenSameMessageId_thenEqualsReturnsTrue() {
        val messageId = listOf("id1", "id2")
        val action1 = GetStatusesAction(messageId)
        val action2 = GetStatusesAction(messageId)

        Assert.assertTrue(action1 == action2)
    }

    @Test
    fun whenDifferentMessageId_thenEqualsReturnsFalse() {
        val action1 = GetStatusesAction(listOf("id1", "id2"))
        val action2 = GetStatusesAction(listOf("id3", "id4"))

        Assert.assertFalse(action1 == action2)
    }

    @Test
    fun whenSameMessageId_thenHashCodeIsEqual() {
        val messageId = listOf("id1", "id2")
        val action1 = GetStatusesAction(messageId)
        val action2 = GetStatusesAction(messageId)

        Assert.assertEquals(action1.hashCode(), action2.hashCode())
    }

    @Test
    fun whenDifferentMessageId_thenHashCodeIsNotEqual() {
        val action1 = GetStatusesAction(listOf("id1", "id2"))
        val action2 = GetStatusesAction(listOf("id3", "id4"))

        Assert.assertNotEquals(action1.hashCode(), action2.hashCode())
    }
}
