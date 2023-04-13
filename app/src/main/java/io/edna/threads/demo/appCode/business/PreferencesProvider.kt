package io.edna.threads.demo.appCode.business

import android.content.Context
import com.google.gson.Gson
import io.edna.threads.demo.appCode.models.UserInfo

class PreferencesProvider(private val context: Context) {
    fun putJsonToPreferences(json: String) {
        context
            .getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit()
            .putString(jsonPreferenceKey, json)
            .commit()
    }

    fun cleanJsonOnPreferences() {
        context
            .getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit()
            .putString(jsonPreferenceKey, "")
            .commit()
    }

    fun saveUserList(value: ArrayList<UserInfo>) {
        val prefsEditor = context.getSharedPreferences(PREF_SERVERS, Context.MODE_PRIVATE).edit()
        prefsEditor.putString(PREF_USER_LIST, Gson().toJson(value))
        prefsEditor.apply()
    }

    fun getAllUserList(): ArrayList<UserInfo> {
        val userListString = context.getSharedPreferences(PREF_SERVERS, Context.MODE_PRIVATE)
            .getString(PREF_USER_LIST, "[]") ?: "[]"
        val userArray: Array<UserInfo> =
            Gson().fromJson(userListString, Array<UserInfo>::class.java)
        val list: ArrayList<UserInfo> = ArrayList()
        list.addAll(userArray)
        return list
    }

    companion object {
        private const val preferenceName = "ecc_demo_json_preference"
        private const val jsonPreferenceKey = "ecc_demo_json_preference_key"
        private const val PREF_SERVERS = "SERVERS_PREFS"
        private const val PREF_USER_LIST = "USER_LIST_PREFS"
    }
}
