package im.threads.ui.preferences

/**
 * Ключи для Preferences уровня UI
 */
object PreferencesUiKeys {
    val APP_LIGHT_STYLE = "APP_LIGHT_STYLE"
    val APP_DARK_STYLE = "APP_DARK_STYLE"
    val STORAGE_PERMISSION_DESCRIPTION_DIALOG_LIGHT_STYLE =
        "STORAGE_PERMISSION_DESCRIPTION_DIALOG_LIGHT_STYLE"
    val STORAGE_PERMISSION_DESCRIPTION_DIALOG_DARK_STYLE =
        "STORAGE_PERMISSION_DESCRIPTION_DIALOG_DARK_STYLE"
    val RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_LIGHT_STYLE =
        "RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_LIGHT_STYLE"
    val RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_DARK_STYLE =
        "RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_DARK_STYLE"
    val CAMERA_PERMISSION_DESCRIPTION_DIALOG_LIGHT_STYLE =
        "CAMERA_PERMISSION_DESCRIPTION_DIALOG_LIGHT_STYLE"
    val CAMERA_PERMISSION_DESCRIPTION_DIALOG_DARK_STYLE =
        "CAMERA_PERMISSION_DESCRIPTION_DIALOG_DARK_STYLE"
    val CLIENT_NOTIFICATION_DISPLAY_TYPE = "CLIENT_NOTIFICATION_DISPLAY_TYPE"
    val PREF_ATTACHMENT_SETTINGS = "PREF_ATTACHMENT_SETTINGS"

    val allPrefKeys = mutableListOf(
        APP_LIGHT_STYLE,
        APP_DARK_STYLE,
        STORAGE_PERMISSION_DESCRIPTION_DIALOG_LIGHT_STYLE,
        STORAGE_PERMISSION_DESCRIPTION_DIALOG_DARK_STYLE,
        RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_LIGHT_STYLE,
        RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_DARK_STYLE,
        CAMERA_PERMISSION_DESCRIPTION_DIALOG_LIGHT_STYLE,
        CAMERA_PERMISSION_DESCRIPTION_DIALOG_DARK_STYLE,
        CLIENT_NOTIFICATION_DISPLAY_TYPE,
        PREF_ATTACHMENT_SETTINGS
    )
}
