package im.threads.business.models

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MessageStatusTest {

    @Test
    fun whenSending_thenOrdinalIsZero() {
        Assert.assertEquals(0, MessageStatus.SENDING.ordinal)
    }

    @Test
    fun whenFailed_thenOrdinalIsOne() {
        Assert.assertEquals(1, MessageStatus.FAILED.ordinal)
    }

    @Test
    fun whenSent_thenOrdinalIsTwo() {
        Assert.assertEquals(2, MessageStatus.SENT.ordinal)
    }

    @Test
    fun whenEnqueued_thenOrdinalIsThree() {
        Assert.assertEquals(3, MessageStatus.ENQUEUED.ordinal)
    }

    @Test
    fun whenDelivered_thenOrdinalIsFour() {
        Assert.assertEquals(4, MessageStatus.DELIVERED.ordinal)
    }

    @Test
    fun whenRead_thenOrdinalIsFive() {
        Assert.assertEquals(5, MessageStatus.READ.ordinal)
    }

    @Test
    fun whenFromOrdinal_thenMessageStatusIsCorrect() {
        Assert.assertEquals(MessageStatus.SENDING, MessageStatus.fromOrdinal(0))
        Assert.assertEquals(MessageStatus.FAILED, MessageStatus.fromOrdinal(1))
        Assert.assertEquals(MessageStatus.SENT, MessageStatus.fromOrdinal(2))
        Assert.assertEquals(MessageStatus.ENQUEUED, MessageStatus.fromOrdinal(3))
        Assert.assertEquals(MessageStatus.DELIVERED, MessageStatus.fromOrdinal(4))
        Assert.assertEquals(MessageStatus.READ, MessageStatus.fromOrdinal(5))
    }

    @Test
    fun whenFromString_thenMessageStatusIsCorrect() {
        Assert.assertEquals(MessageStatus.SENDING, MessageStatus.fromString("sending"))
        Assert.assertEquals(MessageStatus.FAILED, MessageStatus.fromString("failed"))
        Assert.assertEquals(MessageStatus.SENT, MessageStatus.fromString("sent"))
        Assert.assertEquals(MessageStatus.ENQUEUED, MessageStatus.fromString("enqueued"))
        Assert.assertEquals(MessageStatus.DELIVERED, MessageStatus.fromString("delivered"))
        Assert.assertEquals(MessageStatus.READ, MessageStatus.fromString("read"))
    }
}
