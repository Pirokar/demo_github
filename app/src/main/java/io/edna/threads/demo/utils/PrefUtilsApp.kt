package io.edna.threads.demo.utils

import android.content.Context
import com.google.gson.Gson
import io.edna.threads.demo.models.ServerConfig

object PrefUtilsApp {
    private const val PREF_SERVERS = "SERVERS_PREFS"
    private const val PREF_SERVERS_LIST = "SERVERS_LIST_PREFS"
    private const val PREF_APP_VERSION = "APP_VERSION"
    private const val PREF_SELECTED_SERVER = "SELECTED_SERVER_PREFS"

    @JvmStatic
    fun saveAppVersion(context: Context, value: String) {
        val prefsEditor = context.getSharedPreferences(PREF_SERVERS, Context.MODE_PRIVATE).edit()
        prefsEditor.putString(PREF_APP_VERSION, value)
        prefsEditor.apply()
    }

    fun getSavedAppVersion(context: Context): String {
        return context.getSharedPreferences(PREF_SERVERS, Context.MODE_PRIVATE)
            .getString(PREF_APP_VERSION, "1.0.0") ?: "1.0.0"
    }

    @JvmStatic
    fun saveServers(context: Context, value: ArrayList<ServerConfig>) {
        val prefsEditor = context.getSharedPreferences(PREF_SERVERS, Context.MODE_PRIVATE).edit()
        prefsEditor.putString(PREF_SERVERS_LIST, Gson().toJson(value))
        prefsEditor.apply()
    }

    fun getAllServers(context: Context): ArrayList<ServerConfig> {
        val configString = context.getSharedPreferences(PREF_SERVERS, Context.MODE_PRIVATE)
            .getString(PREF_SERVERS_LIST, "[]") ?: "[]"
        val userArray: Array<ServerConfig> =
            Gson().fromJson(configString, Array<ServerConfig>::class.java)
        val list: ArrayList<ServerConfig> = ArrayList()
        list.addAll(userArray)
        return list
    }
}
