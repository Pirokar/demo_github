package im.threads.ui.utils

import android.annotation.SuppressLint
import im.threads.business.chatUpdates.ChatUpdateProcessor
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna
import im.threads.business.preferences.Preferences
import im.threads.business.serviceLocator.core.inject
import im.threads.business.transport.models.AttachmentSettings
import im.threads.ui.preferences.PreferencesUiKeys
import io.reactivex.disposables.Disposable

/**
 * Вспомогательный класс для работы с файлами
 */
@SuppressLint("CheckResult")
object FileHelper {
    private val chatUpdateProcessor: ChatUpdateProcessor by inject()
    private val preferences: Preferences by inject()

    private const val MEGABYTE = (1024 * 1024).toDouble()
    private var disposable: Disposable? = null

    init {
        subscribeToAttachments()
    }

    fun subscribeToAttachments() {
        if (disposable == null || disposable?.isDisposed == true) {
            disposable = chatUpdateProcessor.attachmentSettingsProcessor
                .subscribe(
                    { receivedAttachmentSettings ->
                        receivedAttachmentSettings.content?.let { attachmentSettings = it }
                    },
                    LoggerEdna::error
                )
        }
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

    fun isFileExtensionsEmpty(): Boolean {
        return attachmentSettings.fileExtensions.isNullOrEmpty()
    }

    fun isJpgAllow(): Boolean {
        return attachmentSettings.fileExtensions?.contains("jpg") == true
    }

    val maxAllowedFileSize: Long
        get() = attachmentSettings.maxSize.toLong()

    private var attachmentSettings: AttachmentSettings.Content
        get() {
            val settingsStr = preferences.get(PreferencesUiKeys.PREF_ATTACHMENT_SETTINGS) ?: ""

            return if (settingsStr.isEmpty()) {
                defaultAttachmentSettings
            } else {
                val attachmentSettingsContent =
                    BaseConfig.getInstance().gson.fromJson(settingsStr, AttachmentSettings.Content::class.java)
                attachmentSettingsContent ?: defaultAttachmentSettings
            }
        }
        set(value) = preferences.save(
            PreferencesUiKeys.PREF_ATTACHMENT_SETTINGS,
            BaseConfig.getInstance().gson.toJson(value)
        )

    private val defaultAttachmentSettings: AttachmentSettings.Content
        get() = AttachmentSettings.Content(30, arrayOf("jpeg", "jpg", "png", "pdf", "doc", "docx", "rtf"))
}