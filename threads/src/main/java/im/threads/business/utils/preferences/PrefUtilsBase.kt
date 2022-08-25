package im.threads.business.utils.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna
import im.threads.business.models.CampaignMessage
import im.threads.business.models.FileDescription
import im.threads.business.transport.CloudMessagingType
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.UUID

@SuppressLint("ApplySharedPref")
object PrefUtilsBase {
    private val keys = PrefUtilsBaseKeys()

    @JvmStatic
    val defaultSharedPreferences: SharedPreferences by lazy {
        try {
            val masterKey =
                MasterKey.Builder(BaseConfig.instance.context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
            EncryptedSharedPreferences.create(
                BaseConfig.instance.context,
                keys.ENCRYPTED_STORE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (exception: GeneralSecurityException) {
            LoggerEdna.error(exception)
            BaseConfig.instance.context.getSharedPreferences(keys.STORE_NAME, Context.MODE_PRIVATE)
        } catch (exception: IOException) {
            LoggerEdna.error(exception)
            BaseConfig.instance.context.getSharedPreferences(keys.STORE_NAME, Context.MODE_PRIVATE)
        }
    }

    @JvmStatic
    var lastCopyText: String?
        get() = defaultSharedPreferences.getString(keys.LAST_COPY_TEXT, null)
        set(text) {
            defaultSharedPreferences
                .edit()
                .putString(keys.LAST_COPY_TEXT, text)
                .commit()
        }

    @JvmStatic
    var userName: String?
        get() = defaultSharedPreferences.getString(keys.CLIENT_NAME, "")
        set(clientName) {
            defaultSharedPreferences
                .edit()
                .putString(keys.CLIENT_NAME, clientName)
                .commit()
        }

    @JvmStatic
    var data: String?
        get() = defaultSharedPreferences.getString(keys.EXTRA_DATA, "")
        set(data) {
            defaultSharedPreferences
                .edit()
                .putString(keys.EXTRA_DATA, data)
                .commit()
        }

    @JvmStatic
    fun setNewClientId(clientId: String) {
        defaultSharedPreferences
            .edit()
            .putString(keys.TAG_NEW_CLIENT_ID, clientId)
            .commit()
    }

    @JvmStatic
    val newClientID: String?
        get() = defaultSharedPreferences.getString(keys.TAG_NEW_CLIENT_ID, null)

    @JvmStatic
    fun setClientId(clientId: String) {
        defaultSharedPreferences
            .edit()
            .putString(keys.TAG_CLIENT_ID, clientId)
            .commit()
    }

    @JvmStatic
    val clientID: String
        get() = defaultSharedPreferences.getString(keys.TAG_CLIENT_ID, "") ?: ""

    @JvmStatic
    fun setClientIdEncrypted(clientIdEncrypted: Boolean) {
        defaultSharedPreferences
            .edit()
            .putBoolean(keys.TAG_CLIENT_ID_ENCRYPTED, clientIdEncrypted)
            .commit()
    }

    @JvmStatic
    val clientIDEncrypted: Boolean
        get() = defaultSharedPreferences.getBoolean(keys.TAG_CLIENT_ID_ENCRYPTED, false)

    @JvmStatic
    var clientIdSignature: String?
        get() = defaultSharedPreferences.getString(keys.CLIENT_ID_SIGNATURE_KEY, "")
        set(clientIdSignature) {
            defaultSharedPreferences
                .edit()
                .putString(keys.CLIENT_ID_SIGNATURE_KEY, clientIdSignature)
                .commit()
        }

    @JvmStatic
    var threadId: Long
        get() = defaultSharedPreferences.getLong(keys.THREAD_ID, -1)
        set(threadId) {
            defaultSharedPreferences
                .edit()
                .putLong(keys.THREAD_ID, threadId)
                .commit()
        }

    @JvmStatic
    var fileDescriptionDraft: FileDescription?
        get() {
            val value = defaultSharedPreferences.getString(keys.FILE_DESCRIPTION_DRAFT, "")
            return if (TextUtils.isEmpty(value)) {
                null
            } else BaseConfig.instance.gson.fromJson(
                value,
                FileDescription::class.java
            )
        }
        set(fileDescriptionDraft) {
            val value = if (fileDescriptionDraft != null) BaseConfig.instance.gson.toJson(
                fileDescriptionDraft
            ) else ""
            defaultSharedPreferences
                .edit()
                .putString(keys.FILE_DESCRIPTION_DRAFT, value)
                .commit()
        }

    @JvmStatic
    var campaignMessage: CampaignMessage?
        get() {
            val value = defaultSharedPreferences.getString(keys.CAMPAIGN_MESSAGE, "")
            return if (TextUtils.isEmpty(value)) {
                null
            } else BaseConfig.instance.gson.fromJson(
                value,
                CampaignMessage::class.java
            )
        }
        set(campaignMessage) {
            defaultSharedPreferences
                .edit()
                .putString(
                    keys.CAMPAIGN_MESSAGE,
                    if (campaignMessage != null) BaseConfig.instance.gson.toJson(campaignMessage) else null
                )
                .commit()
        }

    @JvmStatic
    val isClientIdEmpty: Boolean
        get() = clientID.isEmpty()

    @JvmStatic
    var appMarker: String?
        get() {
            val appMarker = defaultSharedPreferences.getString(keys.APP_MARKER_KEY, "") ?: ""
            return appMarker.ifEmpty { null }
        }
        set(appMarker) {
            defaultSharedPreferences
                .edit()
                .putString(keys.APP_MARKER_KEY, appMarker)
                .commit()
        }

    @JvmStatic
    var fcmToken: String?
        get() {
            val fcmToken = defaultSharedPreferences.getString(keys.FCM_TOKEN, "") ?: ""
            return fcmToken.ifEmpty { null }
        }
        set(fcmToken) {
            if (cloudMessagingType == null) {
                cloudMessagingType = CloudMessagingType.FCM.toString()
            }
            defaultSharedPreferences
                .edit()
                .putString(keys.FCM_TOKEN, fcmToken)
                .commit()
        }

    @JvmStatic
    var hcmToken: String?
        get() {
            val hcmToken = defaultSharedPreferences.getString(keys.HCM_TOKEN, "") ?: ""
            return hcmToken.ifEmpty { null }
        }
        set(hcmToken) {
            if (cloudMessagingType == null) {
                cloudMessagingType = CloudMessagingType.HCM.toString()
            }
            defaultSharedPreferences
                .edit()
                .putString(keys.HCM_TOKEN, hcmToken)
                .commit()
        }

    @JvmStatic
    var cloudMessagingType: String?
        get() {
            val cloudMessagingType =
                defaultSharedPreferences.getString(keys.CLOUD_MESSAGING_TYPE, "")
            return if (!TextUtils.isEmpty(cloudMessagingType)) cloudMessagingType else null
        }
        set(cloudMessagingType) {
            defaultSharedPreferences
                .edit()
                .putString(keys.CLOUD_MESSAGING_TYPE, cloudMessagingType)
                .commit()
        }

    @JvmStatic
    var deviceAddress: String?
        get() {
            val deviceAddress = defaultSharedPreferences.getString(keys.DEVICE_ADDRESS, "") ?: ""
            return deviceAddress.ifEmpty { null }
        }
        set(deviceAddress) {
            defaultSharedPreferences
                .edit()
                .putString(keys.DEVICE_ADDRESS, deviceAddress)
                .commit()
        }

    @JvmStatic
    @get:Synchronized
    val deviceUid: String
        get() {
            var deviceUid = defaultSharedPreferences.getString(keys.DEVICE_UID, "") ?: ""
            if (deviceUid.isEmpty()) {
                deviceUid = UUID.randomUUID().toString()
                defaultSharedPreferences
                    .edit()
                    .putString(keys.DEVICE_UID, deviceUid)
                    .commit()
            }
            return deviceUid
        }

    @JvmStatic
    var authToken: String?
        get() = defaultSharedPreferences.getString(keys.AUTH_TOKEN, "")
        set(authToken) {
            defaultSharedPreferences
                .edit()
                .putString(keys.AUTH_TOKEN, authToken)
                .commit()
        }

    @JvmStatic
    var authSchema: String?
        get() = defaultSharedPreferences.getString(keys.AUTH_SCHEMA, "")
        set(authSchema) {
            defaultSharedPreferences
                .edit()
                .putString(keys.AUTH_SCHEMA, authSchema)
                .commit()
        }

    @JvmStatic
    var unreadPushCount: Int
        get() = defaultSharedPreferences.getInt(keys.UNREAD_PUSH_COUNT, 0)
        set(unreadPushCount) {
            defaultSharedPreferences.edit().putInt(keys.UNREAD_PUSH_COUNT, unreadPushCount)
                .commit()
        }
}
