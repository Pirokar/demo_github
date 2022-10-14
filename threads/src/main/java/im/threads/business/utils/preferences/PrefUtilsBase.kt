package im.threads.business.utils.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.transport.CloudMessagingType
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.UUID

@SuppressLint("ApplySharedPref")
internal object PrefUtilsBase {
    @JvmStatic
    val defaultSharedPreferences: SharedPreferences by lazy {
        try {
            val masterKey =
                MasterKey.Builder(BaseConfig.instance.context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
            EncryptedSharedPreferences.create(
                BaseConfig.instance.context,
                PreferencesCoreKeys.ENCRYPTED_STORE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (exception: GeneralSecurityException) {
            LoggerEdna.error(exception)
            BaseConfig.instance.context.getSharedPreferences(PreferencesCoreKeys.STORE_NAME, Context.MODE_PRIVATE)
        } catch (exception: IOException) {
            LoggerEdna.error(exception)
            BaseConfig.instance.context.getSharedPreferences(PreferencesCoreKeys.STORE_NAME, Context.MODE_PRIVATE)
        }
    }

    @JvmStatic
    var fcmToken: String?
        get() {
            val fcmToken = defaultSharedPreferences.getString(PreferencesCoreKeys.FCM_TOKEN, "") ?: ""
            return fcmToken.ifEmpty { null }
        }
        set(fcmToken) {
            if (cloudMessagingType == null) {
                cloudMessagingType = CloudMessagingType.FCM.toString()
            }
            defaultSharedPreferences
                .edit()
                .putString(PreferencesCoreKeys.FCM_TOKEN, fcmToken)
                .commit()
        }

    @JvmStatic
    var hcmToken: String?
        get() {
            val hcmToken = defaultSharedPreferences.getString(PreferencesCoreKeys.HCM_TOKEN, "") ?: ""
            return hcmToken.ifEmpty { null }
        }
        set(hcmToken) {
            if (cloudMessagingType == null) {
                cloudMessagingType = CloudMessagingType.HCM.toString()
            }
            defaultSharedPreferences
                .edit()
                .putString(PreferencesCoreKeys.HCM_TOKEN, hcmToken)
                .commit()
        }

    @JvmStatic
    var cloudMessagingType: String?
        get() {
            val cloudMessagingType =
                defaultSharedPreferences.getString(PreferencesCoreKeys.CLOUD_MESSAGING_TYPE, "")
            return if (!TextUtils.isEmpty(cloudMessagingType)) cloudMessagingType else null
        }
        set(cloudMessagingType) {
            defaultSharedPreferences
                .edit()
                .putString(PreferencesCoreKeys.CLOUD_MESSAGING_TYPE, cloudMessagingType)
                .commit()
        }

    @JvmStatic
    var deviceAddress: String?
        get() {
            val deviceAddress = defaultSharedPreferences.getString(PreferencesCoreKeys.DEVICE_ADDRESS, "") ?: ""
            return deviceAddress.ifEmpty { null }
        }
        set(deviceAddress) {
            defaultSharedPreferences
                .edit()
                .putString(PreferencesCoreKeys.DEVICE_ADDRESS, deviceAddress)
                .commit()
        }

    @JvmStatic
    @get:Synchronized
    val deviceUid: String
        get() {
            var deviceUid = defaultSharedPreferences.getString(PreferencesCoreKeys.DEVICE_UID, "") ?: ""
            if (deviceUid.isEmpty()) {
                deviceUid = UUID.randomUUID().toString()
                defaultSharedPreferences
                    .edit()
                    .putString(PreferencesCoreKeys.DEVICE_UID, deviceUid)
                    .commit()
            }
            return deviceUid
        }

    @JvmStatic
    var unreadPushCount: Int
        get() = defaultSharedPreferences.getInt(PreferencesCoreKeys.UNREAD_PUSH_COUNT, 0)
        set(unreadPushCount) {
            defaultSharedPreferences.edit().putInt(PreferencesCoreKeys.UNREAD_PUSH_COUNT, unreadPushCount)
                .commit()
        }
}
