package im.threads.business.models

import androidx.core.util.ObjectsCompat
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SimpleSystemMessageTest {

    @Test
    fun whenGetType_thenCorrect() {
        val simpleSystemMessage = SimpleSystemMessage("uuid", "type", 0L, "text", 0L)
        Assert.assertEquals("type", simpleSystemMessage.getType())
    }

    @Test
    fun whenGetText_thenCorrect() {
        val simpleSystemMessage = SimpleSystemMessage("uuid", "type", 0L, "text", 0L)
        Assert.assertEquals("text", simpleSystemMessage.getText())
    }

    @Test
    fun whenIsTheSameItem_thenCorrect() {
        val simpleSystemMessage1 = SimpleSystemMessage("uuid", "type", 0L, "text", 0L)
        val simpleSystemMessage2 = SimpleSystemMessage("uuid", "type", 0L, "text", 0L)
        Assert.assertTrue(simpleSystemMessage1.isTheSameItem(simpleSystemMessage2))

        val chatItem = object : ChatItem {
            override val timeStamp: Long = 0L
            override fun isTheSameItem(otherItem: ChatItem?): Boolean = false
            override val threadId: Long? = null
        }
        Assert.assertFalse(simpleSystemMessage1.isTheSameItem(chatItem))
    }

    @Test
    fun whenEquals_thenCorrect() {
        val simpleSystemMessage1 = SimpleSystemMessage("uuid", "type", 0L, "text", 0L)
        val simpleSystemMessage2 = SimpleSystemMessage("uuid", "type", 0L, "text", 0L)
        Assert.assertTrue(simpleSystemMessage1 == simpleSystemMessage2)

        val simpleSystemMessage3 = SimpleSystemMessage("different uuid", "type", 0L, "text", 0L)
        Assert.assertFalse(simpleSystemMessage1 == simpleSystemMessage3)
    }

    @Test
    fun whenHashCode_thenCorrect() {
        val simpleSystemMessage = SimpleSystemMessage("uuid", "type", 0L, "text", 0L)
        val expectedHashCode = ObjectsCompat.hash("uuid", "type", 0L, "text", 0L)
        Assert.assertEquals(expectedHashCode, simpleSystemMessage.hashCode())
    }
}
