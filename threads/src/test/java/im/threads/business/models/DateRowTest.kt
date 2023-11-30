package im.threads.business.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DateRowTest {

    private val dateRow = DateRow(timeStamp = 123456789L)

    @Test
    fun whenToString_thenReturnsCorrectString() {
        val expectedString = "DateRow{date=123456789}"
        assertEquals(expectedString, dateRow.toString())
    }

    @Test
    fun whenIsTheSameItemWithSameClass_thenReturnsTrue() {
        val otherItem = DateRow(timeStamp = 987654321L)
        assertTrue(dateRow.isTheSameItem(otherItem))
    }

    @Test
    fun whenGetThreadId_thenReturnsNull() {
        assertEquals(null, dateRow.threadId)
    }
}
