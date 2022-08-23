package im.threads.internal.model

import im.threads.business.models.CampaignMessage
import im.threads.business.models.FileDescription
import im.threads.business.models.Quote

data class UpcomingUserMessage(
    val fileDescription: FileDescription?,
    val campaignMessage: CampaignMessage?,
    val quote: Quote?,
    val text: String?,
    val copied: Boolean
)
