package im.threads.business.models

import androidx.core.util.ObjectsCompat
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class ScheduleInfoTest {

    @Test
    fun whenIsTheSameItem_thenCorrect() {
        val scheduleInfo1 = ScheduleInfo()
        val scheduleInfo2 = ScheduleInfo()
        Assert.assertTrue(scheduleInfo1.isTheSameItem(scheduleInfo2))

        val chatItem = object : ChatItem {
            override val timeStamp: Long = 0L
            override fun isTheSameItem(otherItem: ChatItem?): Boolean = false
            override val threadId: Long? = null
        }
        Assert.assertFalse(scheduleInfo1.isTheSameItem(chatItem))
    }

    @Test
    fun whenEquals_thenCorrect() {
        val scheduleInfo1 = ScheduleInfo()
        val scheduleInfo2 = ScheduleInfo()
        Assert.assertTrue(scheduleInfo1 == scheduleInfo2)

        val scheduleInfo3 = ScheduleInfo()
        scheduleInfo3.id = 1L
        Assert.assertFalse(scheduleInfo1 == scheduleInfo3)
    }

    @Test
    fun whenHashCode_thenCorrect() {
        val scheduleInfo = ScheduleInfo()
        val expectedHashCode = ObjectsCompat.hash(
            scheduleInfo.id,
            scheduleInfo.notification,
            scheduleInfo.sendDuringInactive,
            scheduleInfo.timeStamp,
            scheduleInfo.startTime,
            scheduleInfo.endTime,
            scheduleInfo.serverTime,
            scheduleInfo.active,
            scheduleInfo.serverTimeDiff
        )
        Assert.assertEquals(expectedHashCode, scheduleInfo.hashCode())
    }

    @Test
    fun whenCalculateServerTimeDiff_thenCorrect() {
        val scheduleInfo = ScheduleInfo()
        scheduleInfo.serverTime = Date()
        scheduleInfo.calculateServerTimeDiff()
        Assert.assertTrue(scheduleInfo.serverTimeDiff >= 0)
    }
}
