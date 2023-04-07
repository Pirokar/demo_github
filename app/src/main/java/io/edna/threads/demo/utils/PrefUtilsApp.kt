package io.edna.threads.demo.utils

import android.content.Context
import com.google.gson.Gson
import io.edna.threads.demo.models.UserInfo

object PrefUtilsApp {
    private const val PREF_SERVERS = "SERVERS_PREFS"
    private const val PREF_USER_LIST = "USER_LIST_PREFS"

    @JvmStatic
    fun saveUserList(context: Context, value: ArrayList<UserInfo>) {
        val prefsEditor = context.getSharedPreferences(PREF_SERVERS, Context.MODE_PRIVATE).edit()
        prefsEditor.putString(PREF_USER_LIST, Gson().toJson(value))
        prefsEditor.apply()
    }

    @JvmStatic
    fun getAllUserList(context: Context): ArrayList<UserInfo> {
        val userListString = context.getSharedPreferences(PREF_SERVERS, Context.MODE_PRIVATE)
            .getString(PREF_USER_LIST, "[]") ?: "[]"
        val userArray: Array<UserInfo> =
            Gson().fromJson(userListString, Array<UserInfo>::class.java)
        val list: ArrayList<UserInfo> = ArrayList()
        list.addAll(userArray)
        return list
    }
}
