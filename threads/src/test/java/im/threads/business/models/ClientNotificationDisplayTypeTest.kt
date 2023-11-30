package im.threads.business.models

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ClientNotificationDisplayTypeTest {

    @Test
    fun whenFromStringWithValidName_thenReturnsCorrectType() {
        assertEquals(ClientNotificationDisplayType.ALL, ClientNotificationDisplayType.fromString("ALL"))
        assertEquals(ClientNotificationDisplayType.CURRENT_THREAD_ONLY, ClientNotificationDisplayType.fromString("CURRENT_THREAD_ONLY"))
        assertEquals(ClientNotificationDisplayType.CURRENT_THREAD_WITH_GROUPING, ClientNotificationDisplayType.fromString("CURRENT_THREAD_WITH_GROUPING"))
    }

    @Test
    fun whenFromStringWithInvalidName_thenReturnsCurrentThreadOnly() {
        assertEquals(ClientNotificationDisplayType.CURRENT_THREAD_ONLY, ClientNotificationDisplayType.fromString("INVALID"))
    }

    @Test
    fun whenFromStringWithNull_thenReturnsCurrentThreadOnly() {
        assertEquals(ClientNotificationDisplayType.CURRENT_THREAD_ONLY, ClientNotificationDisplayType.fromString(null))
    }
}
