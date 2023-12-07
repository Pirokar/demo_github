package im.threads.business.useractivity

import im.threads.business.time.TimeSource
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

class UserActivityTimeImplTest {
    private lateinit var timeSource: TimeSource
    private lateinit var userActivityTime: UserActivityTimeImpl

    @Before
    fun setup() {
        timeSource = Mockito.mock(TimeSource::class.java)
        userActivityTime = UserActivityTimeImpl(timeSource)
    }

    @Test
    fun whenUpdateLastUserActivityTime_thenLastActivityTimeIsUpdated() {
        `when`(timeSource.getCurrentTime()).thenReturn(1000L)
        userActivityTime.updateLastUserActivityTime()
        assertEquals(0, userActivityTime.getSecondsSinceLastActivity())
    }

    @Test
    fun whenGetSecondsSinceLastActivity_thenCorrectTimeIsReturned() {
        `when`(timeSource.getCurrentTime()).thenReturn(2000L)
        userActivityTime.updateLastUserActivityTime()
        `when`(timeSource.getCurrentTime()).thenReturn(3000L)
        assertEquals(1, userActivityTime.getSecondsSinceLastActivity())
    }
}
