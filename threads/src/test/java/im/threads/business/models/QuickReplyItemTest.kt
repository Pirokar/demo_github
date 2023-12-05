package im.threads.business.models

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class QuickReplyItemTest {

    @Test
    fun whenThreadId_thenAlwaysNull() {
        val quickReplyItem = QuickReplyItem(emptyList(), 0L)
        Assert.assertNull(quickReplyItem.threadId)
    }

    @Test
    fun whenIsTheSameItem_thenCorrect() {
        val quickReplyItem = QuickReplyItem(emptyList(), 0L)
        Assert.assertTrue(quickReplyItem.isTheSameItem(QuickReplyItem(emptyList(), 0L)))
        Assert.assertFalse(quickReplyItem.isTheSameItem(null))
    }
}
