package im.threads.business.utils

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
class DateHelperTest {

    @Test
    fun whenGetMessageTimestampFromDateStringWithValidDate_thenReturnsCorrectTimestamp() {
        val dateString = "2022-12-31T23:59:59.000Z"
        val timestamp = DateHelper.getMessageTimestampFromDateString(dateString)
        assert(1672531199000 == timestamp)
    }

    @Test
    fun whenGetMessageTimestampFromDateStringWithInvalidDate_thenReturnsCurrentTimestamp() {
        val dateString = "invalid date"
        val before = System.currentTimeMillis()
        val timestamp = DateHelper.getMessageTimestampFromDateString(dateString)
        val after = System.currentTimeMillis()
        assert(timestamp in before..after)
    }

    @Test
    fun whenGetMessageTimestampFromDateStringWithNull_thenReturnsCurrentTimestamp() {
        val before = System.currentTimeMillis()
        val timestamp = DateHelper.getMessageTimestampFromDateString(null)
        val after = System.currentTimeMillis()
        assert(timestamp in before..after)
    }

    @Test
    fun whenGetMessageDateStringFromTimestampWithValidTimestamp_thenReturnsCorrectDate() {
        val timestamp = 1672444799000
        val dateString = DateHelper.getMessageDateStringFromTimestamp(timestamp)
        assert("2022-12-30T23:59:59.000Z" == dateString)
    }

    @Test
    fun whenGetMessageDateStringFromTimestampWithZero_thenReturnsEpoch() {
        val dateString = DateHelper.getMessageDateStringFromTimestamp(0)
        assert("1970-01-01T00:00:00.000Z" == dateString)
    }

    @Test
    fun whenGetMessageDateStringFromTimestampWithCurrentTime_thenReturnsCurrentDate() {
        val timestamp = System.currentTimeMillis()
        val dateString = DateHelper.getMessageDateStringFromTimestamp(timestamp)
        val expectedDateString = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(timestamp))
        assert(expectedDateString == dateString)
    }
}
