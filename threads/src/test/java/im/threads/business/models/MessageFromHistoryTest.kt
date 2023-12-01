package im.threads.business.models

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.text.SimpleDateFormat
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
class MessageFromHistoryTest {

    @Test
    fun whenGetTimeStamp_thenCorrectTimeStamp() {
        val testDateString = "2023-12-01T11:35:49.110Z"
        val expectedDateString = "2023-12-01T14:35:49.110Z" // utc +3
        val message = MessageFromHistory()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

        message.receivedDate = testDateString

        val date = Calendar.getInstance().apply {
            timeInMillis = message.timeStamp
        }
        val installedDateString = simpleDateFormat.format(date.time)
        assert(installedDateString == expectedDateString)
    }

    @Test
    fun whenIsTheSameItem_thenCorrectResult() {
        val message1 = MessageFromHistory()
        message1.uuid = "123"
        val message2 = MessageFromHistory()
        message2.uuid = "123"
        assertTrue(message1.isTheSameItem(message2))
    }

    @Test
    fun whenIsTheSameItemWithDifferentUUID_thenFalse() {
        val message1 = MessageFromHistory()
        message1.uuid = "123"
        val message2 = MessageFromHistory()
        message2.uuid = "456"
        assertFalse(message1.isTheSameItem(message2))
    }

    @Test
    fun whenEqualsWithNull_thenFalse() {
        val message = MessageFromHistory()
        assertFalse(message.equals(null))
    }

    @Test
    fun whenEqualsWithDifferentClass_thenFalse() {
        val message = MessageFromHistory()
        assertFalse(message.equals("string"))
    }

    @Test
    fun whenEqualsWithSameValues_thenTrue() {
        val message1 = MessageFromHistory()
        message1.uuid = "123"
        val message2 = MessageFromHistory()
        message2.uuid = "123"
        assertTrue(message1 == message2)
    }

    @Test
    fun whenEqualsWithDifferentValues_thenFalse() {
        val message1 = MessageFromHistory()
        message1.uuid = "123"
        val message2 = MessageFromHistory()
        message2.uuid = "456"
        assertFalse(message1 == message2)
    }
}
