package im.threads.business.models
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConsultTypingTest {

    private val consultTyping = ConsultTyping(
        consultId = "Test Consult ID",
        timeStamp = 123456789L,
        avatarPath = "Test Avatar Path"
    )

    @Test
    fun whenIsTheSameItemWithSameClass_thenReturnsTrue() {
        val otherItem = ConsultTyping(
            consultId = "Other Consult ID",
            timeStamp = 987654321L,
            avatarPath = "Other Avatar Path"
        )
        assertTrue(consultTyping.isTheSameItem(otherItem))
    }

    @Test
    fun whenGetThreadId_thenReturnsNull() {
        assertEquals(null, consultTyping.threadId)
    }
}
