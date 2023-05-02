package im.threads.business.models

data class UpcomingUserMessage(
    val fileDescription: FileDescription?,
    val campaignMessage: CampaignMessage?,
    val quote: Quote?,
    val text: String?,
    val copied: Boolean
)
