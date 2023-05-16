package io.edna.threads.demo.appCode.business

import android.content.Context
import android.util.Log
import io.edna.threads.demo.appCode.models.ServerConfig
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream

class ServersProvider(
    private val context: Context,
    private val preferences: PreferencesProvider
) {
    fun readServersFromFile(): ArrayList<ServerConfig> {
        val inputStream: InputStream = context.assets.open("servers_config.json")
        val content = StringBuilder()
        val reader = BufferedReader(inputStream.reader())
        inputStream.use { stream ->
            kotlin.runCatching {
                var line = reader.readLine()
                while (line != null) {
                    content.append(line.trim())
                    line = reader.readLine()
                }
                stream.close()
            }
        }
        Log.i("ServersContent", content.toString())
        val jsonArray = JSONObject(content.toString()).getJSONArray("servers")
        val servers = ArrayList<ServerConfig>(jsonArray.length())
        for (i in 0 until jsonArray.length()) {
            val jsonObj = jsonArray.getJSONObject(i)
            servers.add(
                ServerConfig(
                    name = jsonObj.getString("name"),
                    threadsGateProviderUid = jsonObj.getString("threadsGateProviderUid"),
                    datastoreUrl = jsonObj.getString("datastoreUrl"),
                    serverBaseUrl = jsonObj.getString("serverBaseUrl"),
                    threadsGateUrl = jsonObj.getString("threadsGateUrl"),
                    isSSLPinningDisabled = jsonObj.getBoolean("isSSLPinningDisabled")
                )
            )
        }
        return servers
    }

    fun saveServersToPreferences(servers: ArrayList<ServerConfig>) {
        preferences.saveServers(servers)
    }

    fun saveSelectedServer(server: ServerConfig) {
        preferences.saveSelectedServer(server)
    }

    fun getSelectedServer(): ServerConfig? {
        val selected = preferences.getSelectedServer()
        return if (selected != null) {
            selected
        } else {
            val servers = readServersFromFile()
            if (servers.isNotEmpty()) {
                servers[0]
            } else {
                null
            }
        }
    }
}
