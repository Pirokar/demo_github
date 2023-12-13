package im.threads.ui.styles

import android.content.Context
import androidx.preference.PreferenceManager
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.ui.ChatStyle
import im.threads.ui.extensions.isDarkThemeOn
import im.threads.ui.styles.permissions.PermissionDescriptionDialogStyle
import im.threads.ui.styles.permissions.PermissionDescriptionType

internal class StyleUseCase(private val preferences: Preferences, private val context: Context) {

    /**
     * Возвращает светлую и темную темы
     */
    val incomingStyle: Pair<ChatStyle?, ChatStyle?>
        get() = Pair(incomingStyleLight, incomingStyleDark)

    fun getIncomingStyle(type: PermissionDescriptionType): PermissionDescriptionDialogStyle? {
        return when (type) {
            PermissionDescriptionType.STORAGE -> getStorageStyle()
            PermissionDescriptionType.RECORD_AUDIO -> getRecordAudioStyle()
            PermissionDescriptionType.CAMERA -> getCameraStyle()
        }
    }

    fun setIncomingLightStyle(style: ChatStyle) {
        incomingStyleLight = style
    }

    fun setIncomingDarkStyle(style: ChatStyle) {
        incomingStyleDark = style
    }

    fun setIncomingStyle(
        type: PermissionDescriptionType,
        style: PermissionDescriptionDialogStyle
    ) {
        when (type) {
            PermissionDescriptionType.STORAGE -> setStorageStyle(style)
            PermissionDescriptionType.RECORD_AUDIO -> setRecordAudioStyle(style)
            PermissionDescriptionType.CAMERA -> setCameraStyle(style)
        }
    }

    fun clearUnusedPreferences() {
        val keys = arrayOf(
            "APP_STYLE",
            "STORAGE_PERMISSION_DESCRIPTION_DIALOG_STYLE",
            "RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_STYLE",
            "CAMERA_PERMISSION_DESCRIPTION_DIALOG_STYLE",
            "CLIENT_NOTIFICATION_DISPLAY_TYPE",
            "PREF_ATTACHMENT_SETTINGS",
            "APP_LIGHT_STYLE",
            "APP_DARK_STYLE",
            "STORAGE_PERMISSION_DESCRIPTION_DIALOG_LIGHT_STYLE",
            "STORAGE_PERMISSION_DESCRIPTION_DIALOG_DARK_STYLE",
            "RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_LIGHT_STYLE",
            "RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_DARK_STYLE",
            "CAMERA_PERMISSION_DESCRIPTION_DIALOG_LIGHT_STYLE",
            "CAMERA_PERMISSION_DESCRIPTION_DIALOG_DARK_STYLE"
        )

        val notEncryptedPrefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        keys.forEach { notEncryptedPrefsEditor.putString(it, null) }
        notEncryptedPrefsEditor.commit()

        val storePrefsEditor = context.getSharedPreferences(PreferencesCoreKeys.STORE_NAME, Context.MODE_PRIVATE).edit()
        keys.forEach { storePrefsEditor.putString(it, null) }
        storePrefsEditor.commit()

        val encryptedPrefsEditor = preferences.sharedPreferences.edit()
        keys.forEach { encryptedPrefsEditor.putString(it, null) }
        encryptedPrefsEditor.commit()
    }

    private fun setStorageStyle(style: PermissionDescriptionDialogStyle) {
        if (context.isDarkThemeOn()) {
            storageChatStyleDark = style
        } else {
            storageChatStyleLight = style
        }
    }

    private fun getStorageStyle(): PermissionDescriptionDialogStyle? {
        return if (context.isDarkThemeOn() && storageChatStyleDark != null) {
            storageChatStyleDark
        } else {
            storageChatStyleLight
        }
    }

    private fun setCameraStyle(style: PermissionDescriptionDialogStyle) {
        if (context.isDarkThemeOn()) {
            cameraChatStyleDark = style
        } else {
            cameraChatStyleLight = style
        }
    }

    private fun getCameraStyle(): PermissionDescriptionDialogStyle? {
        return if (context.isDarkThemeOn() && cameraChatStyleDark != null) {
            cameraChatStyleDark
        } else {
            cameraChatStyleLight
        }
    }

    private fun setRecordAudioStyle(style: PermissionDescriptionDialogStyle) {
        if (context.isDarkThemeOn()) {
            recordAudioChatStyleDark = style
        } else {
            recordAudioChatStyleLight = style
        }
    }

    private fun getRecordAudioStyle(): PermissionDescriptionDialogStyle? {
        return if (context.isDarkThemeOn() && recordAudioChatStyleDark != null) {
            recordAudioChatStyleDark
        } else {
            recordAudioChatStyleLight
        }
    }

    companion object {
        private var incomingStyleLight: ChatStyle? = null
        private var incomingStyleDark: ChatStyle? = null
        private var storageChatStyleLight: PermissionDescriptionDialogStyle? = null
        private var cameraChatStyleLight: PermissionDescriptionDialogStyle? = null
        private var recordAudioChatStyleLight: PermissionDescriptionDialogStyle? = null
        private var storageChatStyleDark: PermissionDescriptionDialogStyle? = null
        private var cameraChatStyleDark: PermissionDescriptionDialogStyle? = null
        private var recordAudioChatStyleDark: PermissionDescriptionDialogStyle? = null
    }
}
