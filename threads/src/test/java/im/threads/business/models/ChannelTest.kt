package im.threads.business.models

import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ChannelTest {
    @Test
    fun whenChannelCreated_thenFieldsAreSetCorrectly() {
        val channel = Channel().apply {
            id = 123L
            type = "Test type"
            address = "Test address"
        }

        assertEquals(123L, channel.id)
        assertEquals("Test type", channel.type)
        assertEquals("Test address", channel.address)
    }
}
