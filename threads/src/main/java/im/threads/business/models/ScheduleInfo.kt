package im.threads.business.models

import androidx.core.util.ObjectsCompat
import java.util.*

/**
 * Информация о расписании
 */
class ScheduleInfo : ChatItem {
    var id: Long? = null
    var date: Long? = null
    var notification: String? = null
    val sendDuringInactive = false
    override var timeStamp: Long = 0
    private var serverTime: Date? = null
    private var active = false
    private var serverTimeDiff: Long = 0
    private val intervals: List<WeekDaySchedule>? = null
    private val startTime: Date? = null
    private val endTime: Date? = null

    fun calculateServerTimeDiff() {
        serverTime?.let {
            serverTimeDiff = currentUtcTime - it.time
        }
    }

    /**
     * @return true, если в данный момент чат работает
     */
    val isChatWorking: Boolean
        get() {
            if (startTime == null || endTime == null || serverTime == null) {
                return active
            }
            val currentServerTime = currentUtcTime - serverTimeDiff

            if (active) {
                // Next unavailability not started yet
                // всегда true т.к. startTime - это дата и время старта ближайшего интервала неактивности чата
                if (currentServerTime < startTime.time) {
                    return true
                }

                // Next unavailability started
                if (currentServerTime > startTime.time && currentServerTime < endTime.time) {
                    return false
                }

                // Next unavailability ended
                if (currentServerTime > endTime.time) {
                    return true
                }
            } else {
                // всегда true т.к. endTime - это дата и время окончания ближайшего(или текущего) интервала неактивности чата
                if (currentServerTime < endTime.time) {
                    return false
                }

                // Unavailability ended, next unavailability not started yet
                if (currentServerTime > endTime.time && currentServerTime < startTime.time) {
                    return true
                }

                // Next unavailability started
                if (currentServerTime > startTime.time) {
                    return true
                }
            }
            return true
        }

    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        return otherItem is ScheduleInfo
    }

    override val threadId: Long?
        get() = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ScheduleInfo
        return sendDuringInactive == that.sendDuringInactive && timeStamp == that.timeStamp && active == that.active && serverTimeDiff == that.serverTimeDiff &&
            ObjectsCompat.equals(id, that.id) &&
            ObjectsCompat.equals(notification, that.notification) &&
            ObjectsCompat.equals(startTime, that.startTime) &&
            ObjectsCompat.equals(endTime, that.endTime) &&
            ObjectsCompat.equals(serverTime, that.serverTime)
    }

    override fun hashCode(): Int {
        return ObjectsCompat.hash(
            id,
            notification,
            sendDuringInactive,
            timeStamp,
            startTime,
            endTime,
            serverTime,
            active,
            serverTimeDiff
        )
    }

    private val currentUtcTime: Long
        get() = Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis

    class WeekDaySchedule(
        val id: Int,
        private val weekDay: Int,
        private val startTime: Long,
        private val endTime: Long
    ) {
        override fun toString(): String {
            return "WeekDaySchedule{" +
                "id=" + id +
                ", weekDay=" + weekDay +
                ", startTime=" + startTime +
                ", endTime='" + endTime + '\'' +
                '}'
        }
    }
}
