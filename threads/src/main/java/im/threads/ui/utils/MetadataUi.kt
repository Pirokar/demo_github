package im.threads.ui.utils

import android.content.Context
import im.threads.business.utils.MetadataBusiness

object MetadataUi {
    private const val ATTACHMENT_ENABLED = "im.threads.attachmentEnabled"

    @JvmStatic
    fun getAttachmentEnabled(context: Context): Boolean? {
        val metaData = MetadataBusiness.getMetaData(context)
        return if (metaData != null && metaData.containsKey(ATTACHMENT_ENABLED)) {
            metaData.getBoolean(ATTACHMENT_ENABLED)
        } else {
            null
        }
    }
}
