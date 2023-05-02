package im.threads.business.utils.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import im.threads.business.UserInfoBuilder
import im.threads.business.logger.LoggerEdna
import im.threads.business.preferences.PrefKeysForMigration
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import java.io.File

open class PreferencesMigrationBase(private val context: Context) : Preferences(context) {
    protected open val keys = PreferencesCoreKeys.allPrefKeys

    fun migrateMainSharedPreferences() {
        val oldSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val notEncryptedPreferences = context.getSharedPreferences(
            PreferencesCoreKeys.STORE_NAME,
            Context.MODE_PRIVATE
        )
        if (oldSharedPreferences.all.isNotEmpty()) {
            moveCurrentOnlyPrefs(
                oldSharedPreferences,
                sharedPreferences
            )
        }
        if (notEncryptedPreferences.all.isNotEmpty()) {
            movePreferences(
                notEncryptedPreferences,
                sharedPreferences
            )
            deletePreferenceWithNameContains(PreferencesCoreKeys.STORE_NAME)
        }
    }

    fun migrateNamedPreferences(preferenceName: String) {
        movePreferences(
            context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE),
            sharedPreferences
        )
        deletePreferenceWithNameContains(preferenceName)
    }

    fun migrateUserInfo() {
        var userInfo: UserInfoBuilder? = null
        val keysForMigration = PrefKeysForMigration()
        val editor = sharedPreferences.edit()
        val stubClientId = "stub"

        keysForMigration.list.forEach { key ->
            if (sharedPreferences.all.keys.contains(key)) {
                if (userInfo == null) userInfo = UserInfoBuilder(stubClientId)
                val value = sharedPreferences.all[key]
                when (key) {
                    keysForMigration.APP_MARKER -> {
                        (value as? String)?.let { userInfo?.setAppMarker(it) }
                    }
                    keysForMigration.TAG_CLIENT_ID -> {
                        (value as? String)?.let { userInfo?.clientId = it }
                    }
                    keysForMigration.AUTH_TOKEN -> {
                        (value as? String)?.let {
                            userInfo?.setAuthData(it, userInfo?.authSchema)
                        }
                    }
                    keysForMigration.AUTH_SCHEMA -> {
                        (value as? String)?.let {
                            userInfo?.setAuthData(userInfo?.authToken, it)
                        }
                    }
                    keysForMigration.CLIENT_ID_SIGNATURE -> {
                        (value as? String)?.let {
                            userInfo?.setClientIdSignature(it)
                        }
                    }
                    keysForMigration.DEFAULT_CLIENT_NAMETITLE_TAG -> {
                        (value as? String)?.let {
                            userInfo?.setUserName(it)
                        }
                    }
                    keysForMigration.EXTRA_DATE -> {
                        (value as? String)?.let {
                            userInfo?.setClientData(it)
                        }
                    }
                    keysForMigration.TAG_CLIENT_ID_ENCRYPTED -> {
                        (value as? Boolean)?.let {
                            userInfo?.clientIdEncrypted = it
                        }
                    }
                }
                editor.remove(key)
            }
        }
        if (userInfo != null && userInfo?.clientId != stubClientId) {
            editor.apply()
            save(PreferencesCoreKeys.USER_INFO, userInfo)
        }
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
            if (keys.contains(key)) {
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
