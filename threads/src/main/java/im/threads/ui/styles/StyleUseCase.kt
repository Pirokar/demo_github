package im.threads.ui.styles

import com.google.gson.JsonSyntaxException
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna
import im.threads.business.preferences.Preferences
import im.threads.ui.ChatStyle
import im.threads.ui.preferences.PreferencesUiKeys
import im.threads.ui.styles.permissions.PermissionDescriptionDialogStyle
import im.threads.ui.styles.permissions.PermissionDescriptionType
import java.io.Serializable

internal class StyleUseCase(private val preferences: Preferences) {

    val incomingStyle: ChatStyle?
        get() = getIncomingStyle(PreferencesUiKeys.APP_STYLE, ChatStyle::class.java)

    fun getIncomingStyle(type: PermissionDescriptionType): PermissionDescriptionDialogStyle? {
        return when (type) {
            PermissionDescriptionType.STORAGE -> getIncomingStyle(
                PreferencesUiKeys.STORAGE_PERMISSION_DESCRIPTION_DIALOG_STYLE,
                PermissionDescriptionDialogStyle::class.java
            )
            PermissionDescriptionType.RECORD_AUDIO -> getIncomingStyle(
                PreferencesUiKeys.RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_STYLE,
                PermissionDescriptionDialogStyle::class.java
            )
            PermissionDescriptionType.CAMERA -> getIncomingStyle(
                PreferencesUiKeys.CAMERA_PERMISSION_DESCRIPTION_DIALOG_STYLE,
                PermissionDescriptionDialogStyle::class.java
            )
        }
    }

    fun setIncomingStyle(style: ChatStyle) {
        setIncomingStyle(PreferencesUiKeys.APP_STYLE, style)
    }

    fun setIncomingStyle(
        type: PermissionDescriptionType,
        style: PermissionDescriptionDialogStyle
    ) {
        when (type) {
            PermissionDescriptionType.STORAGE -> setIncomingStyle(
                PreferencesUiKeys.STORAGE_PERMISSION_DESCRIPTION_DIALOG_STYLE,
                style
            )
            PermissionDescriptionType.RECORD_AUDIO -> setIncomingStyle(
                PreferencesUiKeys.RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_STYLE,
                style
            )
            PermissionDescriptionType.CAMERA -> setIncomingStyle(
                PreferencesUiKeys.CAMERA_PERMISSION_DESCRIPTION_DIALOG_STYLE,
                style
            )
        }
    }

    private fun <T : Serializable?> getIncomingStyle(
        key: String,
        styleClass: Class<T>
    ): T? {
        var style: T? = null
        try {
            val sharedPreferencesString = preferences.get<String>(key)
            if (sharedPreferencesString != null) {
                style = BaseConfig.instance.gson.fromJson(sharedPreferencesString, styleClass)
            }
        } catch (ex: IllegalStateException) {
            LoggerEdna.error("getIncomingStyle ${styleClass.canonicalName} failed: ", ex)
        } catch (ex: JsonSyntaxException) {
            LoggerEdna.error("getIncomingStyle ${styleClass.canonicalName} failed: ", ex)
        }
        return style
    }

    private fun <T : Serializable?> setIncomingStyle(key: String, style: T) {
        preferences.save(key, BaseConfig.instance.gson.toJson(style))
    }
}
