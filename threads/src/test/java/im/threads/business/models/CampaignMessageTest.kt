package im.threads.business.models

import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class CampaignMessageTest {
    private val dateFormat = SimpleDateFormat(CAMPAIGN_DATE_FORMAT, Locale.getDefault())

    @Test
    fun whenCampaignMessageCreated_thenFieldsAreSetCorrectly() {
        val date = Date()
        val campaignMessage = CampaignMessage(
            text = "Test text",
            senderName = "Test sender",
            receivedDate = date,
            chatMessageId = "Test chat message id",
            gateMessageId = 123L,
            expiredAt = date,
            skillId = 456,
            campaign = "Test campaign",
            priority = 789
        )

        assertEquals("Test text", campaignMessage.text)
        assertEquals("Test sender", campaignMessage.senderName)
        assertEquals(dateFormat.format(date), dateFormat.format(campaignMessage.receivedDate))
        assertEquals("Test chat message id", campaignMessage.chatMessageId)
        assertEquals(123L, campaignMessage.gateMessageId)
        assertEquals(dateFormat.format(date), dateFormat.format(campaignMessage.expiredAt))
        assertEquals(456, campaignMessage.skillId)
        assertEquals("Test campaign", campaignMessage.campaign)
        assertEquals(789, campaignMessage.priority)
    }
}
