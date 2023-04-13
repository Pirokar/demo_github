package im.threads.ui.styles

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna
import im.threads.business.preferences.Preferences
import im.threads.ui.ChatStyle
import im.threads.ui.extensions.isDarkThemeOn
import im.threads.ui.preferences.PreferencesUiKeys
import im.threads.ui.styles.permissions.PermissionDescriptionDialogStyle
import im.threads.ui.styles.permissions.PermissionDescriptionType
import java.io.Serializable

internal class StyleUseCase(private val preferences: Preferences, private val context: Context) {
    private val gson = Gson()

    /**
     * Вовзаращает светлую и темную темы
     */
    val incomingStyle: Pair<ChatStyle?, ChatStyle?>
        get() = Pair(
            getIncomingStyle(PreferencesUiKeys.APP_LIGHT_STYLE, ChatStyle::class.java),
            getIncomingStyle(PreferencesUiKeys.APP_DARK_STYLE, ChatStyle::class.java)
        )

    fun getIncomingStyle(type: PermissionDescriptionType): PermissionDescriptionDialogStyle? {
        return when (type) {
            PermissionDescriptionType.STORAGE -> getIncomingStyle(
                storagePermissionDescriptionDialogStyleKey,
                PermissionDescriptionDialogStyle::class.java
            )
            PermissionDescriptionType.RECORD_AUDIO -> getIncomingStyle(
                recordAudioPermissionDescriptionDialogStyleKey,
                PermissionDescriptionDialogStyle::class.java
            )
            PermissionDescriptionType.CAMERA -> getIncomingStyle(
                cameraPermissionDescriptionDialogStyleKey,
                PermissionDescriptionDialogStyle::class.java
            )
        }
    }

    fun setIncomingLightStyle(style: ChatStyle) {
        setIncomingStyle(PreferencesUiKeys.APP_LIGHT_STYLE, style)
    }

    fun setIncomingDarkStyle(style: ChatStyle) {
        setIncomingStyle(PreferencesUiKeys.APP_DARK_STYLE, style)
    }

    fun setIncomingStyle(
        type: PermissionDescriptionType,
        style: PermissionDescriptionDialogStyle
    ) {
        when (type) {
            PermissionDescriptionType.STORAGE -> setIncomingStyle(
                storagePermissionDescriptionDialogStyleKey,
                style
            )
            PermissionDescriptionType.RECORD_AUDIO -> setIncomingStyle(
                recordAudioPermissionDescriptionDialogStyleKey,
                style
            )
            PermissionDescriptionType.CAMERA -> setIncomingStyle(
                cameraPermissionDescriptionDialogStyleKey,
                style
            )
        }
    }

    private val storagePermissionDescriptionDialogStyleKey: String
        get() {
            return if (context.isDarkThemeOn()) {
                PreferencesUiKeys.STORAGE_PERMISSION_DESCRIPTION_DIALOG_DARK_STYLE
            } else {
                PreferencesUiKeys.STORAGE_PERMISSION_DESCRIPTION_DIALOG_LIGHT_STYLE
            }
        }

    private val recordAudioPermissionDescriptionDialogStyleKey: String
        get() {
            return if (context.isDarkThemeOn()) {
                PreferencesUiKeys.RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_DARK_STYLE
            } else {
                PreferencesUiKeys.RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_LIGHT_STYLE
            }
        }

    private val cameraPermissionDescriptionDialogStyleKey: String
        get() {
            return if (context.isDarkThemeOn()) {
                PreferencesUiKeys.CAMERA_PERMISSION_DESCRIPTION_DIALOG_DARK_STYLE
            } else {
                PreferencesUiKeys.CAMERA_PERMISSION_DESCRIPTION_DIALOG_LIGHT_STYLE
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
        preferences.save(key, gson.toJson(style))
    }
}
