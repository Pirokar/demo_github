package im.threads.ui.utils

import android.content.Context
import im.threads.business.utils.MetadataBusiness

object MetadataUi {
    private const val ATTACHMENT_ENABLED = "im.threads.attachmentEnabled"
    private const val FILES_AND_MEDIA_MENU_ITEM_ENABLED = "im.threads.filesAndMediaMenuItemEnabled"

    @JvmStatic
    fun getAttachmentEnabled(context: Context): Boolean? {
        val metaData = MetadataBusiness.getMetaData(context)
        return if (metaData != null && metaData.containsKey(ATTACHMENT_ENABLED)) {
            metaData.getBoolean(ATTACHMENT_ENABLED)
        } else null
    }

    @JvmStatic
    fun getFilesAndMediaMenuItemEnabled(context: Context): Boolean {
        val metaData = MetadataBusiness.getMetaData(context)
        return if (metaData != null && metaData.containsKey(FILES_AND_MEDIA_MENU_ITEM_ENABLED)) {
            metaData.getBoolean(FILES_AND_MEDIA_MENU_ITEM_ENABLED)
        } else true
    }
}
