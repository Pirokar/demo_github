package im.threads.internal.model

data class UpcomingUserMessage(
    val fileDescription: FileDescription?,
    val campaignMessage: CampaignMessage?,
    val quote: Quote?,
    val text: String?,
    val copyied: Boolean
)
