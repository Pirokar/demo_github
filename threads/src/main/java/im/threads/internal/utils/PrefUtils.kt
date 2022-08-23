package im.threads.internal.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.JsonSyntaxException
import im.threads.ChatStyle
import im.threads.business.logger.LoggerEdna
import im.threads.business.models.CampaignMessage
import im.threads.business.models.FileDescription
import im.threads.business.transport.CloudMessagingType
import im.threads.internal.Config
import im.threads.internal.model.ClientNotificationDisplayType
import im.threads.styles.permissions.PermissionDescriptionDialogStyle
import im.threads.styles.permissions.PermissionDescriptionType
import java.io.File
import java.io.IOException
import java.io.Serializable
import java.security.GeneralSecurityException
import java.util.UUID

@SuppressLint("ApplySharedPref")
class PrefUtils private constructor() {
    companion object {
        // styles
        private const val APP_STYLE = "APP_STYLE"
        private const val STORAGE_PERMISSION_DESCRIPTION_DIALOG_STYLE =
            "STORAGE_PERMISSION_DESCRIPTION_DIALOG_STYLE"
        private const val RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_STYLE =
            "RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_STYLE"
        private const val CAMERA_PERMISSION_DESCRIPTION_DIALOG_STYLE =
            "CAMERA_PERMISSION_DESCRIPTION_DIALOG_STYLE"
        private const val TAG_CLIENT_ID = "TAG_CLIENT_ID"
        private const val TAG_CLIENT_ID_ENCRYPTED = "TAG_CLIENT_ID_ENCRYPTED"
        private const val CLIENT_ID_SIGNATURE_KEY = "CLIENT_ID_SIGNATURE"
        private const val TAG_NEW_CLIENT_ID = "TAG_NEW_CLIENT_ID"
        private const val CLIENT_NAME = "DEFAULT_CLIENT_NAMETITLE_TAG"
        private const val EXTRA_DATA = "EXTRA_DATE"
        private const val LAST_COPY_TEXT = "LAST_COPY_TEXT"
        private const val APP_MARKER_KEY = "APP_MARKER"
        private const val DEVICE_ADDRESS = "DEVICE_ADDRESS"
        private const val FCM_TOKEN = "FCM_TOKEN"
        private const val HCM_TOKEN = "HCM_TOKEN"
        private const val CLOUD_MESSAGING_TYPE = "CLOUD_MESSAGING_TYPE"
        private const val DEVICE_UID = "DEVICE_UID"
        private const val AUTH_TOKEN = "AUTH_TOKEN"
        private const val AUTH_SCHEMA = "AUTH_SCHEMA"
        private const val CLIENT_NOTIFICATION_DISPLAY_TYPE = "CLIENT_NOTIFICATION_DISPLAY_TYPE"
        private const val THREAD_ID = "THREAD_ID"
        private const val FILE_DESCRIPTION_DRAFT = "FILE_DESCRIPTION_DRAFT"
        private const val CAMPAIGN_MESSAGE = "CAMPAIGN_MESSAGE"
        private const val PREF_ATTACHMENT_SETTINGS = "PREF_ATTACHMENT_SETTINGS"
        private const val UNREAD_PUSH_COUNT = "UNREAD_PUSH_COUNT"
        private const val STORE_NAME = "im.threads.internal.utils.PrefStore"
        private const val ENCRYPTED_STORE_NAME = "im.threads.internal.utils.EncryptedPrefStore"

        private val allPrefKeys = arrayListOf(
            APP_STYLE,
            STORAGE_PERMISSION_DESCRIPTION_DIALOG_STYLE,
            RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_STYLE,
            CAMERA_PERMISSION_DESCRIPTION_DIALOG_STYLE,
            TAG_CLIENT_ID,
            TAG_CLIENT_ID_ENCRYPTED,
            CLIENT_ID_SIGNATURE_KEY,
            TAG_NEW_CLIENT_ID,
            CLIENT_NAME,
            EXTRA_DATA,
            LAST_COPY_TEXT,
            APP_MARKER_KEY,
            DEVICE_ADDRESS,
            FCM_TOKEN,
            HCM_TOKEN,
            CLOUD_MESSAGING_TYPE,
            DEVICE_UID,
            AUTH_TOKEN,
            AUTH_SCHEMA,
            CLIENT_NOTIFICATION_DISPLAY_TYPE,
            THREAD_ID,
            FILE_DESCRIPTION_DRAFT,
            CAMPAIGN_MESSAGE,
            PREF_ATTACHMENT_SETTINGS,
            UNREAD_PUSH_COUNT,
            STORE_NAME,
            ENCRYPTED_STORE_NAME
        )

        @JvmStatic
        val defaultSharedPreferences: SharedPreferences by lazy {
            try {
                val masterKey =
                    MasterKey.Builder(Config.instance.context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build()
                EncryptedSharedPreferences.create(
                    Config.instance.context,
                    ENCRYPTED_STORE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (exception: GeneralSecurityException) {
                LoggerEdna.error(exception)
                Config.instance.context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
            } catch (exception: IOException) {
                LoggerEdna.error(exception)
                Config.instance.context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
            }
        }

        @JvmStatic
        var lastCopyText: String?
            get() = defaultSharedPreferences.getString(LAST_COPY_TEXT, null)
            set(text) {
                defaultSharedPreferences
                    .edit()
                    .putString(LAST_COPY_TEXT, text)
                    .commit()
            }

        @JvmStatic
        var userName: String?
            get() = defaultSharedPreferences.getString(CLIENT_NAME, "")
            set(clientName) {
                defaultSharedPreferences
                    .edit()
                    .putString(CLIENT_NAME, clientName)
                    .commit()
            }

        @JvmStatic
        var data: String?
            get() = defaultSharedPreferences.getString(EXTRA_DATA, "")
            set(data) {
                defaultSharedPreferences
                    .edit()
                    .putString(EXTRA_DATA, data)
                    .commit()
            }

        @JvmStatic
        fun setNewClientId(clientId: String) {
            defaultSharedPreferences
                .edit()
                .putString(TAG_NEW_CLIENT_ID, clientId)
                .commit()
        }

        @JvmStatic
        val newClientID: String?
            get() = defaultSharedPreferences.getString(TAG_NEW_CLIENT_ID, null)

        @JvmStatic
        fun setClientId(clientId: String) {
            defaultSharedPreferences
                .edit()
                .putString(TAG_CLIENT_ID, clientId)
                .commit()
        }

        @JvmStatic
        val clientID: String
            get() = defaultSharedPreferences.getString(TAG_CLIENT_ID, "") ?: ""

        @JvmStatic
        fun setClientIdEncrypted(clientIdEncrypted: Boolean) {
            defaultSharedPreferences
                .edit()
                .putBoolean(TAG_CLIENT_ID_ENCRYPTED, clientIdEncrypted)
                .commit()
        }

        @JvmStatic
        val clientIDEncrypted: Boolean
            get() = defaultSharedPreferences.getBoolean(TAG_CLIENT_ID_ENCRYPTED, false)

        @JvmStatic
        var clientIdSignature: String?
            get() = defaultSharedPreferences.getString(CLIENT_ID_SIGNATURE_KEY, "")
            set(clientIdSignature) {
                defaultSharedPreferences
                    .edit()
                    .putString(CLIENT_ID_SIGNATURE_KEY, clientIdSignature)
                    .commit()
            }

        @JvmStatic
        var clientNotificationDisplayType: ClientNotificationDisplayType
            get() = ClientNotificationDisplayType.fromString(
                defaultSharedPreferences.getString(CLIENT_NOTIFICATION_DISPLAY_TYPE, "")
            )
            set(type) {
                defaultSharedPreferences
                    .edit()
                    .putString(CLIENT_NOTIFICATION_DISPLAY_TYPE, type.name)
                    .commit()
            }

        @JvmStatic
        var threadId: Long
            get() = defaultSharedPreferences.getLong(THREAD_ID, -1)
            set(threadId) {
                defaultSharedPreferences
                    .edit()
                    .putLong(THREAD_ID, threadId)
                    .commit()
            }

        @JvmStatic
        var attachmentSettings: String?
            get() = defaultSharedPreferences.getString(PREF_ATTACHMENT_SETTINGS, "")
            set(settings) {
                defaultSharedPreferences
                    .edit()
                    .putString(PREF_ATTACHMENT_SETTINGS, settings)
                    .commit()
            }

        @JvmStatic
        var fileDescriptionDraft: FileDescription?
            get() {
                val value = defaultSharedPreferences.getString(FILE_DESCRIPTION_DRAFT, "")
                return if (TextUtils.isEmpty(value)) {
                    null
                } else Config.instance.gson.fromJson(
                    value,
                    FileDescription::class.java
                )
            }
            set(fileDescriptionDraft) {
                val value = if (fileDescriptionDraft != null) Config.instance.gson.toJson(
                    fileDescriptionDraft
                ) else ""
                defaultSharedPreferences
                    .edit()
                    .putString(FILE_DESCRIPTION_DRAFT, value)
                    .commit()
            }

        @JvmStatic
        var campaignMessage: CampaignMessage?
            get() {
                val value = defaultSharedPreferences.getString(CAMPAIGN_MESSAGE, "")
                return if (TextUtils.isEmpty(value)) {
                    null
                } else Config.instance.gson.fromJson(
                    value,
                    CampaignMessage::class.java
                )
            }
            set(campaignMessage) {
                defaultSharedPreferences
                    .edit()
                    .putString(
                        CAMPAIGN_MESSAGE,
                        if (campaignMessage != null) Config.instance.gson.toJson(campaignMessage) else null
                    )
                    .commit()
            }

        @JvmStatic
        val isClientIdEmpty: Boolean
            get() = clientID.isEmpty()

        @JvmStatic
        val incomingStyle: ChatStyle?
            get() = getIncomingStyle(APP_STYLE, ChatStyle::class.java)

        @JvmStatic
        fun getIncomingStyle(
            type: PermissionDescriptionType
        ): PermissionDescriptionDialogStyle? {
            return when (type) {
                PermissionDescriptionType.STORAGE -> getIncomingStyle(
                    STORAGE_PERMISSION_DESCRIPTION_DIALOG_STYLE,
                    PermissionDescriptionDialogStyle::class.java
                )
                PermissionDescriptionType.RECORD_AUDIO -> getIncomingStyle(
                    RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_STYLE,
                    PermissionDescriptionDialogStyle::class.java
                )
                PermissionDescriptionType.CAMERA -> getIncomingStyle(
                    CAMERA_PERMISSION_DESCRIPTION_DIALOG_STYLE,
                    PermissionDescriptionDialogStyle::class.java
                )
            }
        }

        @JvmStatic
        fun setIncomingStyle(style: ChatStyle) {
            setIncomingStyle(APP_STYLE, style)
        }

        @JvmStatic
        fun setIncomingStyle(
            type: PermissionDescriptionType,
            style: PermissionDescriptionDialogStyle
        ) {
            when (type) {
                PermissionDescriptionType.STORAGE -> setIncomingStyle(
                    STORAGE_PERMISSION_DESCRIPTION_DIALOG_STYLE,
                    style
                )
                PermissionDescriptionType.RECORD_AUDIO -> setIncomingStyle(
                    RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_STYLE,
                    style
                )
                PermissionDescriptionType.CAMERA -> setIncomingStyle(
                    CAMERA_PERMISSION_DESCRIPTION_DIALOG_STYLE,
                    style
                )
            }
        }

        @JvmStatic
        var appMarker: String?
            get() {
                val appMarker = defaultSharedPreferences.getString(APP_MARKER_KEY, "") ?: ""
                return appMarker.ifEmpty { null }
            }
            set(appMarker) {
                defaultSharedPreferences
                    .edit()
                    .putString(APP_MARKER_KEY, appMarker)
                    .commit()
            }

        @JvmStatic
        var fcmToken: String?
            get() {
                val fcmToken = defaultSharedPreferences.getString(FCM_TOKEN, "") ?: ""
                return fcmToken.ifEmpty { null }
            }
            set(fcmToken) {
                val cloudMessagingType = cloudMessagingType
                if (cloudMessagingType == null) {
                    Companion.cloudMessagingType = CloudMessagingType.FCM.toString()
                }
                defaultSharedPreferences
                    .edit()
                    .putString(FCM_TOKEN, fcmToken)
                    .commit()
            }

        @JvmStatic
        var hcmToken: String?
            get() {
                val hcmToken = defaultSharedPreferences.getString(HCM_TOKEN, "") ?: ""
                return hcmToken.ifEmpty { null }
            }
            set(hcmToken) {
                val cloudMessagingType = cloudMessagingType
                if (cloudMessagingType == null) {
                    Companion.cloudMessagingType = CloudMessagingType.HCM.toString()
                }
                defaultSharedPreferences
                    .edit()
                    .putString(HCM_TOKEN, hcmToken)
                    .commit()
            }

        @JvmStatic
        var cloudMessagingType: String?
            get() {
                val cloudMessagingType =
                    defaultSharedPreferences.getString(CLOUD_MESSAGING_TYPE, "")
                return if (!TextUtils.isEmpty(cloudMessagingType)) cloudMessagingType else null
            }
            set(cloudMessagingType) {
                defaultSharedPreferences
                    .edit()
                    .putString(CLOUD_MESSAGING_TYPE, cloudMessagingType)
                    .commit()
            }

        @JvmStatic
        var deviceAddress: String?
            get() {
                val deviceAddress = defaultSharedPreferences.getString(DEVICE_ADDRESS, "") ?: ""
                return deviceAddress.ifEmpty { null }
            }
            set(deviceAddress) {
                defaultSharedPreferences
                    .edit()
                    .putString(DEVICE_ADDRESS, deviceAddress)
                    .commit()
            }

        @JvmStatic
        @get:Synchronized
        val deviceUid: String
            get() {
                var deviceUid = defaultSharedPreferences.getString(DEVICE_UID, "") ?: ""
                if (deviceUid.isEmpty()) {
                    deviceUid = UUID.randomUUID().toString()
                    defaultSharedPreferences
                        .edit()
                        .putString(DEVICE_UID, deviceUid)
                        .commit()
                }
                return deviceUid
            }

        @JvmStatic
        var authToken: String?
            get() = defaultSharedPreferences.getString(AUTH_TOKEN, "")
            set(authToken) {
                defaultSharedPreferences
                    .edit()
                    .putString(AUTH_TOKEN, authToken)
                    .commit()
            }

        @JvmStatic
        var authSchema: String?
            get() = defaultSharedPreferences.getString(AUTH_SCHEMA, "")
            set(authSchema) {
                defaultSharedPreferences
                    .edit()
                    .putString(AUTH_SCHEMA, authSchema)
                    .commit()
            }

        @JvmStatic
        fun migrateMainSharedPreferences() {
            val context = Config.instance.context
            val oldSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val notEncryptedPreferences = context.getSharedPreferences(
                STORE_NAME,
                Context.MODE_PRIVATE
            )
            if (oldSharedPreferences.all.isNotEmpty()) {
                moveCurrentOnlyPrefs(oldSharedPreferences, defaultSharedPreferences)
            }
            if (notEncryptedPreferences.all.isNotEmpty()) {
                movePreferences(notEncryptedPreferences, defaultSharedPreferences)
                deletePreferenceWithNameContains(STORE_NAME)
            }
        }

        @JvmStatic
        fun migrateNamedPreferences(preferenceName: String) {
            movePreferences(
                Config.instance.context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE),
                defaultSharedPreferences
            )
            deletePreferenceWithNameContains(preferenceName)
        }

        @JvmStatic
        var unreadPushCount: Int
            get() = defaultSharedPreferences.getInt(UNREAD_PUSH_COUNT, 0)
            set(unreadPushCount) {
                defaultSharedPreferences.edit().putInt(UNREAD_PUSH_COUNT, unreadPushCount)
                    .commit()
            }

        private fun <T : Serializable?> getIncomingStyle(
            key: String,
            styleClass: Class<T>
        ): T? {
            var style: T? = null
            try {
                val sharedPreferences = defaultSharedPreferences
                if (sharedPreferences.getString(key, null) != null) {
                    val sharedPreferencesString = sharedPreferences.getString(key, null)
                    style = Config.instance.gson.fromJson(sharedPreferencesString, styleClass)
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
            defaultSharedPreferences
                .edit()
                .putString(key, Config.instance.gson.toJson(style))
                .commit()
        }

        private fun movePreferences(fromPrefs: SharedPreferences, toPrefs: SharedPreferences) {
            val editor = toPrefs.edit()
            for ((key, prefsValue) in fromPrefs.all) {
                addPrefToEditor(key, prefsValue, editor)
            }
            editor.commit()
            fromPrefs.edit().clear().commit()
        }

        private fun moveCurrentOnlyPrefs(fromPrefs: SharedPreferences, toPrefs: SharedPreferences) {
            val editor = toPrefs.edit()
            val keysToDelete = arrayListOf<String>()
            for ((key, prefsValue) in fromPrefs.all) {
                if (allPrefKeys.contains(key)) {
                    addPrefToEditor(key, prefsValue, editor)
                    keysToDelete.add(key)
                }
            }
            val fromPrefsEditor = fromPrefs.edit()
            keysToDelete.forEach { fromPrefsEditor.remove(it) }
            editor.commit()
            fromPrefsEditor.commit()
        }

        private fun addPrefToEditor(key: String, value: Any?, editor: SharedPreferences.Editor) {
            when (value) {
                is Boolean -> {
                    editor.putBoolean(key, value)
                }
                is Float -> {
                    editor.putFloat(key, value)
                }
                is Int -> {
                    editor.putInt(key, value)
                }
                is Long -> {
                    editor.putLong(key, value)
                }
                is String -> {
                    editor.putString(key, value)
                }
            }
        }

        private fun deletePreferenceWithNameContains(nameContains: String) {
            val context = Config.instance.context
            try {
                context.filesDir.parent?.let { parentPath ->
                    val dir = File("$parentPath/shared_prefs/")
                    val children = dir.list()
                    if (children != null) {
                        for (child in children) {
                            @Suppress("DEPRECATION")
                            if (child.contains(nameContains)) {
                                File(dir, child).delete()
                            }
                        }
                    }
                }
            } catch (exception: Exception) {
                LoggerEdna.error("Error when deleting preference file", exception)
            }
        }
    }
}
