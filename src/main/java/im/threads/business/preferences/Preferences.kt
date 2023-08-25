package im.threads.business.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import im.threads.business.logger.LoggerEdna
import im.threads.business.preferences.encrypted.EncryptedSharedPreferences
import im.threads.business.preferences.encrypted.MasterKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.reflect.Type
import java.security.GeneralSecurityException

open class Preferences(private val context: Context) {
    private val storeName = "im.threads.internal.utils.PrefStore"
    private val encryptedStoreName = "im.threads.internal.utils.EncryptedPrefStore"

    /**
     * Шифрованный контейнер для хранения настроек
     */
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

    val preferencesStartKeysCount: Int by lazy { sharedPreferences.all.keys.size }

    val coroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     * Предоставляет настройку в соответствии с переданным ключом и типом.
     * При необходимости производит миграцию, если такой ключ присутствует в списке,
     * но его тип отличается от хранимого.
     * @param key ключ для получения данных
     * @param default значение по умолчанию, если не удалось получить значение
     */
    private inline fun <reified T : Any> getFromPreferencesFile(key: String, default: T? = null): T? {
        val returnType: Type = object : TypeToken<T>() {}.type

        @Suppress("CommitPrefEdits")
        return try {
            val ret: String? = sharedPreferences.getString(key, null)
            val value = Gson().fromJson(ret, returnType) ?: default ?: throw NullPointerException()
            if (value == "null") null else value
        } catch (exc: Exception) {
            if (sharedPreferences.all.keys.contains(key)) {
                val value = sharedPreferences.all.getValue(key)
                if (value is T) {
                    sharedPreferences.edit().remove(key)
                    save(key, value)
                    return value
                } else {
                    return default
                }
            } else {
                return default
            }
        }
    }

    /**
     * Сохраняет настройку в map по ключу. Возможно синхронное и асинхронное сохранение.
     * @param key ключ для сохранения данных
     * @param obj сохраняемый объект
     */
    inline fun <reified T : Any> save(key: String, obj: T?) {
        val json = if (obj != null) Gson().toJson(obj).toString() else null
        savePreferenceToRam(key, json)

        coroutineScope.launch {
            val editor = sharedPreferences.edit()
            editor.putString(key, json)
            editor.commit()
        }
    }

    /**
     * Предоставляет настройку в соответствии с переданным ключом и типом.
     * @param key ключ для получения данных
     * @param default значение по умолчанию, если не удалось получить значение
     */
    inline fun <reified T : Any> get(key: String, default: T? = null): T? {
        val returnType: Type = object : TypeToken<T>() {}.type
        return try {
            var ret: String = getPreferenceFromRam(key)
            if (ret.isEmpty() && !isRamPreferencesLoaded) {
                ret = sharedPreferences.getString(key, null) ?: ""
                if (ret.isNotEmpty()) savePreferenceToRam(key, ret)
            }
            val value = Gson().fromJson(ret, returnType) ?: default ?: throw NullPointerException()
            if (value == "null") null else value
        } catch (exc: Exception) {
            null
        }
    }

    internal fun loadPreferencesInRam() {
        sharedPreferences.all.keys.forEach {
            savePreferenceToRam(it, getFromPreferencesFile(it) ?: "")
        }
        isRamPreferencesLoaded = true
    }

    private fun onGetEncryptedPreferencesException(context: Context, exc: Exception): SharedPreferences {
        LoggerEdna.error(exc)
        return context.getSharedPreferences(storeName, Context.MODE_PRIVATE)
    }

    companion object {
        private val ramPreferences = HashMap<String, String>()
        var isRamPreferencesLoaded = false

        @Synchronized
        fun savePreferenceToRam(key: String, value: String?) {
            ramPreferences[key] = value ?: ""
        }

        fun getPreferenceFromRam(key: String) = ramPreferences[key] ?: ""
    }
}
