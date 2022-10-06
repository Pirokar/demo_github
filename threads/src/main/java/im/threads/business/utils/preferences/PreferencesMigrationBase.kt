package im.threads.business.utils.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import im.threads.business.logger.LoggerEdna
import im.threads.business.preferences.PreferencesImpl
import java.io.File

open class PreferencesMigrationBase(private val context: Context) : PreferencesImpl(context) {
    protected open val keys = PrefUtilsBaseKeys()

    fun migrateMainSharedPreferences() {
        val oldSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val notEncryptedPreferences = context.getSharedPreferences(
            keys.STORE_NAME,
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
            deletePreferenceWithNameContains(keys.STORE_NAME)
        }
    }

    fun migrateNamedPreferences(preferenceName: String) {
        movePreferences(
            context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE),
            sharedPreferences
        )
        deletePreferenceWithNameContains(preferenceName)
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
            if (keys.allPrefKeys.contains(key)) {
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
