package im.threads.ui.styles

import android.content.Context
import im.threads.ui.ChatStyle
import im.threads.ui.extensions.isDarkThemeOn
import im.threads.ui.styles.permissions.PermissionDescriptionDialogStyle
import im.threads.ui.styles.permissions.PermissionDescriptionType

internal class StyleUseCase(private val context: Context) {

    /**
     * Вовзаращает светлую и темную темы
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
