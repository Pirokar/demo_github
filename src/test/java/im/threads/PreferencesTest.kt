package im.threads

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import im.threads.business.UserInfoBuilder
import im.threads.business.preferences.PrefKeysForMigration
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.utils.preferences.PreferencesMigrationBase
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PreferencesTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val preferences = Preferences(context)
    private val keys = listOf(
        "boolean",
        "string",
        "long",
        "int",
        "double",
        "object",
        "oldBoolean",
        "oldInt"
    )

    @After
    fun after() {
        val editor = preferences.sharedPreferences.edit()
        keys.forEach {
            editor.remove(it)
        }
        editor.commit()
    }

    @Test
    fun whenBooleanStored_thenReadSuccessfully() {
        val value = true
        preferences.save(keys[0], value)
        assert(preferences.get<Boolean>(keys[0]) == value)
    }

    @Test
    fun whenStringStored_thenReadSuccessfully() {
        val value = "testString"
        preferences.save(keys[1], value)
        assert(preferences.get<String>(keys[1]) == value)
    }

    @Test
    fun whenLongStored_thenReadSuccessfully() {
        val value = 1042435245345L
        preferences.save(keys[2], value)
        assert(preferences.get<Long>(keys[2]) == value)
    }

    @Test
    fun whenIntStored_thenReadSuccessfully() {
        val value = 1042
        preferences.save(keys[3], value)
        assert(preferences.get<Int>(keys[3]) == value)
    }

    @Test
    fun whenDoubleStored_thenReadSuccessfully() {
        val value = 500.0
        preferences.save(keys[4], value)
        assert(preferences.get<Double>(keys[4]) == value)
    }

    @Test
    fun whenObjectStored_thenReadSuccessfully() {
        val value = UserInfoBuilder("1000").apply {
            setAuthData("token", "schema")
            setClientData("Some client data")
            setClientIdSignature("signature")
            setUserName("userName")
            setAppMarker("AppMarker")
            setClientIdEncrypted(true)
        }

        preferences.save(keys[5], value)
        assert(preferences.get<UserInfoBuilder>(keys[5])?.equals(value) == true)
    }

    @Test
    fun whenOldUserInfoExist_thenTestMigration() {
        val migrationKeys = PrefKeysForMigration()
        val value = "testValue"
        val booleanValue = true
        val resultObject = UserInfoBuilder(value).apply {
            setAuthData(value, value)
            setClientData(value)
            setClientIdSignature(value)
            setUserName(value)
            setAppMarker(value)
            setClientIdEncrypted(booleanValue)
        }

        val editor = preferences.sharedPreferences.edit()
        migrationKeys.list.forEach {
            if (it != migrationKeys.TAG_CLIENT_ID_ENCRYPTED) {
                editor.putString(it, value)
            }
        }
        editor.putBoolean(migrationKeys.TAG_CLIENT_ID_ENCRYPTED, booleanValue)
        editor.commit()

        val migration = PreferencesMigrationBase(context)
        migration.migrateUserInfo()
        val migratedResult = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)
        assert(migratedResult?.equals(resultObject) == true)
    }

    @Test
    fun whenOldBooleanExist_thenGetMigratedValue() {
        val value = true
        preferences.sharedPreferences.edit().putBoolean(keys[6], value).commit()
        assert(preferences.get<Boolean>(keys[6]) == value)
    }

    @Test
    fun whenOldIntExist_thenGetMigratedValue() {
        val value = 1456
        preferences.sharedPreferences.edit().putInt(keys[7], value).commit()
        assert(preferences.get<Int>(keys[7]) == value)
    }
}
