package io.edna.threads.demo.appCode.business

import android.content.Context
import com.google.gson.Gson
import im.threads.business.logger.LoggerEdna
import io.edna.threads.demo.appCode.extensions.fromJson
import io.edna.threads.demo.appCode.extensions.toJson
import io.edna.threads.demo.appCode.models.ServerConfig
import io.edna.threads.demo.appCode.models.UserInfo
import java.io.File

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
        val prefsEditor = context.getSharedPreferences(PREF_DEMO, Context.MODE_PRIVATE).edit()
        prefsEditor.putString(PREF_USER_LIST, Gson().toJson(value))
        prefsEditor.apply()
    }

    fun getAllUserList(): ArrayList<UserInfo> {
        val userListString = context.getSharedPreferences(PREF_DEMO, Context.MODE_PRIVATE)
            .getString(PREF_USER_LIST, "[]") ?: "[]"
        val userArray: Array<UserInfo> =
            Gson().fromJson(userListString, Array<UserInfo>::class.java)
        val list: ArrayList<UserInfo> = ArrayList()
        list.addAll(userArray)
        return list
    }

    fun saveAppVersion(value: String) {
        val prefsEditor = context.getSharedPreferences(PREF_DEMO, Context.MODE_PRIVATE).edit()
        prefsEditor.putString(PREF_APP_VERSION, value)
        prefsEditor.apply()
    }

    fun getSavedAppVersion(): String {
        return context.getSharedPreferences(PREF_DEMO, Context.MODE_PRIVATE)
            .getString(PREF_APP_VERSION, "1.0.0") ?: "1.0.0"
    }

    fun saveSelectedUser(value: UserInfo) {
        val prefsEditor = context.getSharedPreferences(PREF_DEMO, Context.MODE_PRIVATE).edit()
        prefsEditor.putString(PREF_SELECTED_USER, Gson().toJson(value))
        prefsEditor.apply()
    }

    fun getSelectedUser(): UserInfo {
        val userString = context.getSharedPreferences(PREF_DEMO, Context.MODE_PRIVATE)
            .getString(PREF_SELECTED_USER, "")
        return Gson().fromJson(userString, UserInfo::class.java) ?: UserInfo()
    }

    fun saveSelectedServer(value: ServerConfig) {
        val prefsEditor = context.getSharedPreferences(PREF_DEMO, Context.MODE_PRIVATE).edit()
        prefsEditor.putString(PREF_SELECTED_SERVER, Gson().toJson(value))
        prefsEditor.apply()
    }

    fun getSelectedServer(): ServerConfig {
        val serverString = context.getSharedPreferences(PREF_DEMO, Context.MODE_PRIVATE)
            .getString(PREF_SELECTED_SERVER, "")
        return Gson().fromJson(serverString, ServerConfig::class.java) ?: ServerConfig()
    }

    fun saveServers(value: ArrayList<ServerConfig>) {
        val prefsEditor = context.getSharedPreferences(PREF_DEMO, Context.MODE_PRIVATE).edit()
        prefsEditor.putString(PREF_SERVERS_LIST, Gson().toJson(value))
        prefsEditor.apply()
    }

    fun getAllServers1(): ArrayList<ServerConfig> {
        val configString = context.getSharedPreferences(PREF_DEMO, Context.MODE_PRIVATE)
            .getString(PREF_SERVERS_LIST, "[]") ?: "[]"
        val serverArray: Array<ServerConfig> =
            Gson().fromJson(configString, Array<ServerConfig>::class.java)
        val list: ArrayList<ServerConfig> = ArrayList()
        list.addAll(serverArray)
        return list
    }







    fun getAllServers(context: Context): Map<String, String> {
        return getServersFrom(context, PREF_SERVERS_LIST)
    }

    fun applyServersFromFile(context: Context) {
        val serversFromApp = getAllServers(context)
            .map { Gson().fromJson<ServerConfig>(it.value) }
            .filter { it.isFromApp }

        val servers = getServersFrom(context, PREF_IMPORTED_FILE_SERVERS_NAME)
            .map { Gson().fromJson<ServerConfig>(it.value) }
            .toMutableList()
        servers.addAll(serversFromApp)
        val serversToSave = servers.associate { it.name to it.toJson() }
        addServers(context, serversToSave, true)
        deletePreferenceWithNameContains(context, PREF_IMPORTED_FILE_SERVERS_NAME)
    }

    fun addServers(context: Context, servers: Map<String?, String>, clearExisting: Boolean = false) {
        val prefsEditor = context.getSharedPreferences(PREF_SERVERS_LIST, Context.MODE_PRIVATE).edit()
        if (clearExisting) prefsEditor.clear()
        servers.forEach { prefsEditor.putString(it.key, it.value) }
        prefsEditor.commit()
    }

    private fun getServersFrom(context: Context, prefsName: String): Map<String, String> {
        return context
            .getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .all as? Map<String, String> ?: HashMap()
    }

    private fun deletePreferenceWithNameContains(context: Context, nameContains: String) {
        try {
            val dir = File(context.filesDir.parent + "/shared_prefs/")
            val children = dir.list()
            if (children != null) {
                for (child in children) {
                    if (child.contains(nameContains)) {
                        File(dir, child).delete()
                    }
                }
            }
        } catch (exception: Exception) {
            LoggerEdna.error("Error when deleting preference file", exception)
        }
    }



    companion object {
        private const val preferenceName = "ecc_demo_json_preference"
        private const val jsonPreferenceKey = "ecc_demo_json_preference_key"
        private const val PREF_DEMO = "DEMO_PREFS"
        private const val PREF_USER_LIST = "USER_LIST_PREFS"
        private const val PREF_SERVERS_LIST = "SERVERS_LIST_PREFS"
        private const val PREF_APP_VERSION = "APP_VERSION"
        private const val PREF_SELECTED_USER = "SELECTED_USER_PREFS"
        private const val PREF_SELECTED_SERVER = "SELECTED_SERVER_PREFS"

        private const val PREF_IMPORTED_FILE_SERVERS_NAME = "servers_config"
    }
}
