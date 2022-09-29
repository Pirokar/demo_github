package im.threads.business.logger.core

import android.os.Process
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Форматирует текст для файлов логгера
 */
internal class LoggerFileFormatter {
    private val timeFormat: ThreadLocal<SimpleDateFormat> =
        object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue(): SimpleDateFormat {
                return SimpleDateFormat("dd-MM HH:mm:ss.SSS", Locale.getDefault())
            }
        }

    private val fileNameFormat: ThreadLocal<SimpleDateFormat> =
        object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue(): SimpleDateFormat {
                return SimpleDateFormat("dd_MM_HH", Locale.getDefault())
            }
        }

    private val date: ThreadLocal<Date> = object : ThreadLocal<Date>() {
        override fun initialValue(): Date {
            return Date()
        }
    }

    // 26-07 14:28:48.889 PROCESS_ID-THREAD_ID LEVEL/TAG: LOG
    private val lineFormat = "%s %d-%d %s/%s: %s"

    /**
     * Форматирует строку лога
     * @param timeInMillis время лога в мс
     * @param level уровень лога согласно [LoggerLevel]
     * @param tag используется для индентификации лога
     * @param log текст лога
     */
    fun formatLine(
        timeInMillis: Long,
        level: String,
        tag: String?,
        log: String
    ): String {
        date.set(Date(timeInMillis))
        return date.get()?.let { date ->
            val timestamp = timeFormat.get()?.format(date)
            val processId = Process.myPid()
            val threadId = Process.myTid()

            String.format(
                Locale.getDefault(),
                lineFormat,
                timestamp,
                processId,
                threadId,
                level,
                tag,
                log
            )
        } ?: "Error when parsing date"
    }

    /**
     * Форматирует имя файла для лога
     * @param timeInMillis время первого лога в мс, будет частью имени файла
     */
    fun formatFileName(timeInMillis: Long): String {
        date.set(Date(timeInMillis))

        return date.get()?.let { date ->
            fileNameFormat.get()?.format(date) + "_00.txt"
        } ?: "Error when parsing date"
    }
}
