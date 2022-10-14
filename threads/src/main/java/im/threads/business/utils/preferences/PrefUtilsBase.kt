package im.threads.business.utils.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna
import im.threads.business.preferences.PreferencesCoreKeys
import java.io.IOException
import java.security.GeneralSecurityException

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
}
