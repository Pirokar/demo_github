package im.threads.business.models

import im.threads.business.models.enums.ErrorStateEnum
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AttachmentTest {
    private val attachment = Attachment()

    @Test
    fun whenNameFieldIsNotNullOrEmpty_thenNameReturnsFieldValue() {
        attachment.name = "Test name"
        assertEquals("Test name", attachment.name)
    }

    @Test
    fun whenNameFieldIsNullOrEmpty_thenNameReturnsOptionalName() {
        val optional = mock(Optional::class.java)
        `when`(optional.name).thenReturn("Optional name")
        attachment.optional = optional
        assertEquals("Optional name", attachment.name)
    }

    @Test
    fun whenTypeFieldIsNotNullOrEmpty_thenTypeReturnsFieldValue() {
        attachment.type = "Test type"
        assertEquals("Test type", attachment.type)
    }

    @Test
    fun whenTypeFieldIsNullOrEmpty_thenTypeReturnsOptionalType() {
        val optional = mock(Optional::class.java)
        `when`(optional.type).thenReturn("Optional type")
        attachment.optional = optional
        assertEquals("Optional type", attachment.type)
    }

    @Test
    fun whenErrorCodeIsNotNull_thenGetErrorCodeStateReturnsErrorStateEnumFromString() {
        attachment.errorCode = "Test error code"
        assertEquals(ErrorStateEnum.errorStateEnumFromString("Test error code"), attachment.getErrorCodeState())
    }

    @Test
    fun whenErrorCodeIsNull_thenGetErrorCodeStateReturnsANY() {
        assertEquals(ErrorStateEnum.ANY, attachment.getErrorCodeState())
    }
}
