package im.threads.business.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import im.threads.business.logger.LoggerEdna
import java.io.IOException
import java.lang.reflect.Type
import java.security.GeneralSecurityException

open class Preferences(private val context: Context) {
    private val storeName = "im.threads.internal.utils.PrefStore"
    private val encryptedStoreName = "im.threads.internal.utils.EncryptedPrefStore"

    val sharedPreferences: SharedPreferences by lazy {
        try {
            val masterKey =
                MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
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

    inline fun <reified T : Any> get(key: String, default: T? = null): T? {
        val ret: String? = sharedPreferences.getString(key, null)
        val returnType: Type = object : TypeToken<T>() {}.type
        return try {
            Gson().fromJson(ret, returnType) ?: default
        } catch (exc: Exception) {
            if (sharedPreferences.all.keys.contains(key)) {
                val value = sharedPreferences.all.getValue(key)
                if (value is T) {
                    sharedPreferences.edit().remove(key)
                    save(key, value, true)
                    return value
                } else {
                    return null
                }
            } else {
                return null
            }
        }
    }

    inline fun <reified T : Any> save(key: String, obj: T?, saveAsync: Boolean = false) {
        val json = Gson().toJson(obj).toString()
        val editor = sharedPreferences.edit()
        editor.putString(key, json)

        if (saveAsync) {
            editor.apply()
        } else {
            editor.commit()
        }
    }

    private fun onGetEncryptedPreferencesException(context: Context, exc: Exception): SharedPreferences {
        LoggerEdna.error(exc)
        return context.getSharedPreferences(storeName, Context.MODE_PRIVATE)
    }
}
