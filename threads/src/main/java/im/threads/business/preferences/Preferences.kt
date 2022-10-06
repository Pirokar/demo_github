package im.threads.business.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna
import java.io.IOException
import java.security.GeneralSecurityException

open class Preferences(private val context: Context) {
    private val storeName = "im.threads.internal.utils.PrefStore"
    private val encryptedStoreName = "im.threads.internal.utils.EncryptedPrefStore"

    protected val sharedPreferences: SharedPreferences by lazy {
        try {
            val masterKey =
                MasterKey.Builder(BaseConfig.instance.context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
            EncryptedSharedPreferences.create(
                context,
                encryptedStoreName,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (exception: GeneralSecurityException) {
            onGetEncryptedPreferencesException(context, exception)
        } catch (exception: IOException) {
            onGetEncryptedPreferencesException(context, exception)
        }
    }

    fun save(key: String, value: Any?) {
        val editor = sharedPreferences.edit()
        if (value == null) {
            editor.remove(key)
        } else {
            when (value) {
                is Boolean,
                is Int,
                is Long,
                is String,
                is Float -> editor.putString(key, value.toString())
                else -> editor.putString(key, Gson().toJson(value))
            }
        }

        editor.commit()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        val value = sharedPreferences.getString(key, "") ?: ""
        (value as? T)?.let { return it }

        return if (value.isNotEmpty()) {
            try {
                Gson().fromJson(value, object : TypeToken<T>() {}.type)
            } catch (exc: Exception) {
                null
            }
        } else {
            null
        }
    }

    private fun onGetEncryptedPreferencesException(context: Context, exc: Exception): SharedPreferences {
        LoggerEdna.error(exc)
        return context.getSharedPreferences(storeName, Context.MODE_PRIVATE)
    }
}
