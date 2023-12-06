package im.threads.business.transport.models

import im.threads.business.models.enums.ErrorStateEnum
import org.junit.Assert.assertEquals
import org.junit.Test

class AttachmentTest {

    @Test
    fun whenGetErrorCodeState_thenReturnsCorrectErrorState() {
        val attachment = Attachment(errorCode = "DS-1")
        assertEquals(ErrorStateEnum.DISALLOWED, attachment.getErrorCodeState())

        val attachment2 = Attachment(errorCode = "DS-10")
        assertEquals(ErrorStateEnum.TIMEOUT, attachment2.getErrorCodeState())

        val attachment3 = Attachment(errorCode = "DS-100")
        assertEquals(ErrorStateEnum.UNEXPECTED, attachment3.getErrorCodeState())

        val attachment4 = Attachment(errorCode = "DS-999")
        assertEquals(ErrorStateEnum.ANY, attachment4.getErrorCodeState())

        val attachment5 = Attachment()
        assertEquals(ErrorStateEnum.ANY, attachment5.getErrorCodeState())
    }
}
