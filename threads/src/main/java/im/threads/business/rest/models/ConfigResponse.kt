package im.threads.business.rest.models

import im.threads.business.transport.models.ContentAttachmentSettings
import im.threads.business.transport.models.ContentScheduleInfoContent

data class ConfigResponse(
    val settings: SettingsResponse? = null,
    val schedule: ContentScheduleInfoContent? = null,
    val attachmentSettings: ContentAttachmentSettings? = null
)
