package io.edna.threads.demo.appCode.business

import android.content.Context
import com.google.gson.Gson
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.models.ServerConfig
import java.io.BufferedReader
import java.io.InputStream

class ServersProvider(
    private val context: Context,
    private val preferences: PreferencesProvider
) {
    fun readServersFromFile(): ArrayList<ServerConfig> {
        val inputStream: InputStream = context.resources.openRawResource(R.raw.servers_config)
        val content = StringBuilder()
        val reader = BufferedReader(inputStream.reader())
        inputStream.use { stream ->
            kotlin.runCatching {
                var line = reader.readLine()
                while (line != null) {
                    content.append(line)
                    line = reader.readLine()
                }
                stream.close()
            }
        }
        val newServers: Array<ServerConfig> =
            Gson().fromJson(content.toString(), Array<ServerConfig>::class.java)
        val list: ArrayList<ServerConfig> = ArrayList()
        list.addAll(newServers)
        return list
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
