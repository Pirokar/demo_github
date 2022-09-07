package im.threads.ui.utils.preferences

import com.google.gson.JsonSyntaxException
import im.threads.ChatStyle
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna
import im.threads.business.models.ClientNotificationDisplayType
import im.threads.business.utils.preferences.PrefUtilsBase
import im.threads.ui.styles.permissions.PermissionDescriptionDialogStyle
import im.threads.ui.styles.permissions.PermissionDescriptionType
import java.io.Serializable

internal object PrefUtilsUi {
    private val keys = PrefUtilsKeys()

    @JvmStatic
    var clientNotificationDisplayType: ClientNotificationDisplayType
        get() = ClientNotificationDisplayType.fromString(
            PrefUtilsBase.defaultSharedPreferences.getString(keys.CLIENT_NOTIFICATION_DISPLAY_TYPE, "")
        )
        set(type) {
            PrefUtilsBase.defaultSharedPreferences
                .edit()
                .putString(keys.CLIENT_NOTIFICATION_DISPLAY_TYPE, type.name)
                .commit()
        }

    @JvmStatic
    var attachmentSettings: String?
        get() = PrefUtilsBase.defaultSharedPreferences.getString(keys.PREF_ATTACHMENT_SETTINGS, "")
        set(settings) {
            PrefUtilsBase.defaultSharedPreferences
                .edit()
                .putString(keys.PREF_ATTACHMENT_SETTINGS, settings)
                .commit()
        }

    @JvmStatic
    val incomingStyle: ChatStyle?
        get() = getIncomingStyle(keys.APP_STYLE, ChatStyle::class.java)

    @JvmStatic
    fun getIncomingStyle(
        type: PermissionDescriptionType
    ): PermissionDescriptionDialogStyle? {
        return when (type) {
            PermissionDescriptionType.STORAGE -> getIncomingStyle(
                keys.STORAGE_PERMISSION_DESCRIPTION_DIALOG_STYLE,
                PermissionDescriptionDialogStyle::class.java
            )
            PermissionDescriptionType.RECORD_AUDIO -> getIncomingStyle(
                keys.RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_STYLE,
                PermissionDescriptionDialogStyle::class.java
            )
            PermissionDescriptionType.CAMERA -> getIncomingStyle(
                keys.CAMERA_PERMISSION_DESCRIPTION_DIALOG_STYLE,
                PermissionDescriptionDialogStyle::class.java
            )
        }
    }

    @JvmStatic
    fun setIncomingStyle(style: ChatStyle) {
        setIncomingStyle(keys.APP_STYLE, style)
    }

    @JvmStatic
    fun setIncomingStyle(
        type: PermissionDescriptionType,
        style: PermissionDescriptionDialogStyle
    ) {
        when (type) {
            PermissionDescriptionType.STORAGE -> setIncomingStyle(
                keys.STORAGE_PERMISSION_DESCRIPTION_DIALOG_STYLE,
                style
            )
            PermissionDescriptionType.RECORD_AUDIO -> setIncomingStyle(
                keys.RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_STYLE,
                style
            )
            PermissionDescriptionType.CAMERA -> setIncomingStyle(
                keys.CAMERA_PERMISSION_DESCRIPTION_DIALOG_STYLE,
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
            val sharedPreferences = PrefUtilsBase.defaultSharedPreferences
            if (sharedPreferences.getString(key, null) != null) {
                val sharedPreferencesString = sharedPreferences.getString(key, null)
                style = BaseConfig.instance.gson.fromJson(sharedPreferencesString, styleClass)
            }
        } catch (ex: IllegalStateException) {
            LoggerEdna.error("getIncomingStyle ${styleClass.canonicalName} failed: ", ex)
        } catch (ex: JsonSyntaxException) {
            LoggerEdna.error("getIncomingStyle ${styleClass.canonicalName} failed: ", ex)
        }
        return style
    }

    private fun <T : Serializable?> setIncomingStyle(
        key: String,
        style: T
    ) {
        PrefUtilsBase.defaultSharedPreferences
            .edit()
            .putString(key, BaseConfig.instance.gson.toJson(style))
            .commit()
    }
}
