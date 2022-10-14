package im.threads.ui.utils

import android.annotation.SuppressLint
import im.threads.business.chat_updates.ChatUpdateProcessor
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna
import im.threads.business.serviceLocator.core.inject
import im.threads.business.transport.models.AttachmentSettings
import im.threads.ui.utils.preferences.PrefUtilsUi
import im.threads.ui.utils.preferences.PrefUtilsUi.attachmentSettings

@SuppressLint("CheckResult")
enum class FileHelper {
    INSTANCE;

    private val chatUpdateProcessor: ChatUpdateProcessor by inject()

    init {
        chatUpdateProcessor.attachmentSettingsProcessor
            .subscribe(
                { attachmentSettings -> saveAttachmentSettings(attachmentSettings.content) },
                LoggerEdna::error
            )
    }

    fun isAllowedFileSize(fileSize: Long): Boolean {
        return fileSize / MEGABYTE <= maxAllowedFileSize
    }

    fun isAllowedFileExtension(fileExtension: String?): Boolean {
        val attachmentSettings = attachmentSettings
        for (allowedExt in attachmentSettings.fileExtensions) {
            if (allowedExt.equals(fileExtension, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    val maxAllowedFileSize: Long
        get() = attachmentSettings.maxSize.toLong()

    private fun saveAttachmentSettings(attachmentSettingsContent: AttachmentSettings.Content) {
        PrefUtilsUi.attachmentSettings = BaseConfig.instance.gson.toJson(attachmentSettingsContent)
    }

    private val attachmentSettings: AttachmentSettings.Content
        get() {
            var settingsStr = PrefUtilsUi.attachmentSettings
            settingsStr = settingsStr ?: ""
            return if (settingsStr.isEmpty()) {
                defaultAttachmentSettings
            } else {
                val attachmentSettingsContent =
                    BaseConfig.instance.gson.fromJson(settingsStr, AttachmentSettings.Content::class.java)
                attachmentSettingsContent ?: defaultAttachmentSettings
            }
        }
    private val defaultAttachmentSettings: AttachmentSettings.Content
        get() = AttachmentSettings.Content(30, arrayOf("jpeg", "jpg", "png", "pdf", "doc", "docx", "rtf"))

    companion object {
        private const val MEGABYTE = (1024 * 1024).toDouble()
    }
}
