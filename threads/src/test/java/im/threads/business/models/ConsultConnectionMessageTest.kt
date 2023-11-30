package im.threads.business.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class ConsultConnectionMessageTest {
    private val consultConnectionMessage = ConsultConnectionMessage(
        uuid = "Test UUID",
        consultId = "Test Consult ID",
        type = "Test Type",
        name = "Test Name",
        sex = true,
        date = Date().time,
        avatarPath = "Test Avatar Path",
        status = "Test Status",
        title = "Test Title",
        orgUnit = "Test Org Unit",
        role = "Test Role",
        displayMessage = true,
        text = "Test Text",
        threadId = 123L
    )

    @Test
    fun whenGetType_thenReturnsType() {
        assertEquals("Test Type", consultConnectionMessage.getType())
    }

    @Test
    fun whenGetText_thenReturnsText() {
        assertEquals("Test Text", consultConnectionMessage.getText())
    }

    @Test
    fun whenIsTheSameItemWithSameUUID_thenReturnsTrue() {
        val otherItem = ConsultConnectionMessage(
            uuid = "Test UUID",
            consultId = "Other Consult ID",
            type = "Other Type",
            name = "Other Name",
            sex = false,
            date = Date().time,
            avatarPath = "Other Avatar Path",
            status = "Other Status",
            title = "Other Title",
            orgUnit = "Other Org Unit",
            role = "Other Role",
            displayMessage = false,
            text = "Other Text",
            threadId = 456L
        )
        assertTrue(consultConnectionMessage.isTheSameItem(otherItem))
    }
}
