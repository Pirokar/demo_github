package im.threads.business.utils

import im.threads.business.logger.LoggerEdna.error
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateHelper {
    private const val SERVER_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    private const val SERVER_DATE_TIMEZONE = "UTC"
    private val sdf = SimpleDateFormat(SERVER_DATE_FORMAT, Locale.getDefault())

    init {
        sdf.timeZone = TimeZone.getTimeZone(SERVER_DATE_TIMEZONE)
    }

    @Synchronized
    fun getMessageTimestampFromDateString(dateString: String?): Long {
        var date = Date()
        try {
            if (dateString != null) {
                date = sdf.parse(dateString) ?: date
            }
        } catch (e: ParseException) {
            error("getMessageTimestampFromDateString", e)
        }
        return date.time
    }

    @Synchronized
    fun getMessageDateStringFromTimestamp(timestamp: Long): String {
        return sdf.format(Date(timestamp))
    }
}
