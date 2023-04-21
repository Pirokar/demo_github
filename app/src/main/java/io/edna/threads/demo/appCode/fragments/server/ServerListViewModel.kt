package io.edna.threads.demo.appCode.fragments.server

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.gson.Gson
import io.edna.threads.demo.BuildConfig
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.PreferencesProvider
import io.edna.threads.demo.appCode.models.ServerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.parceler.Parcels
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStream

class ServerListViewModel(
    private val preferencesProvider: PreferencesProvider
) : ViewModel(), DefaultLifecycleObserver {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var _serverListLiveData = MutableLiveData(preferencesProvider.getAllServers())
    var serverConfigLiveData: LiveData<ArrayList<ServerConfig>> = _serverListLiveData

    fun click(view: View) {
        val navigationController: NavController =
            (view.context as Activity).findNavController(R.id.nav_host_fragment_content_main)
        when (view.id) {
            R.id.backButton -> navigationController.navigate(R.id.action_ServersFragment_to_LaunchFragment)
            R.id.addServer -> {
                navigationController.navigate(R.id.action_ServerListFragment_to_AddServerFragment)
            }
        }
    }

    fun backToLaunchScreen(context: Context?) {
        if (context != null) {
            val navigationController: NavController =
                (context as Activity).findNavController(R.id.nav_host_fragment_content_main)
            navigationController.navigate(R.id.action_ServersFragment_to_LaunchFragment)
        }
    }

    private fun addConfig(config: ServerConfig) {
        coroutineScope.launch {
            addServer(config)
        }
    }

    fun removeConfig(config: ServerConfig) {
        coroutineScope.launch {
            removeServer(config)
        }
    }

    fun copyServersFromFileIfNeed(context: Context) {
        if (preferencesProvider.getSavedAppVersion() != BuildConfig.VERSION_NAME) {
            coroutineScope.launch {
                copyServersFromFile(context)
            }
        } else {
            _serverListLiveData.postValue(preferencesProvider.getAllServers())
        }
    }

    fun callFragmentResultListener(key: String, bundle: Bundle) {
        if (key == ServerListFragment.SERVER_CONFIG_KEY && bundle.containsKey(ServerListFragment.SERVER_CONFIG_KEY)) {
            val config: ServerConfig? = if (Build.VERSION.SDK_INT >= 33) {
                Parcels.unwrap(
                    bundle.getParcelable(
                        ServerListFragment.SERVER_CONFIG_KEY,
                        Parcelable::class.java
                    )
                )
            } else {
                Parcels.unwrap(bundle.getParcelable(ServerListFragment.SERVER_CONFIG_KEY))
            }
            if (config != null) {
                addConfig(config)
            }
        }
    }

    private fun removeServer(config: ServerConfig) =
        coroutineScope.launch {
            val srcServers = preferencesProvider.getAllServers()
            val finalServers = removeServers(srcServers, config)
            _serverListLiveData.postValue(finalServers)
            preferencesProvider.saveServers(finalServers)
        }

    private fun addServer(config: ServerConfig) =
        coroutineScope.launch {
            val servers = preferencesProvider.getAllServers()
            val finalServers = updateServers(servers, config)
            _serverListLiveData.postValue(finalServers)
            preferencesProvider.saveServers(finalServers)
        }

/*    private fun copyServersFromFile(context: Context) {
        val newServers = readServersFromFile(context)
        val oldServers = preferencesProvider.getAllServers()
        val servers = updateOldServers(oldServers, newServers)
        _serverListLiveData.postValue(servers)
        preferencesProvider.saveServers(servers)
        preferencesProvider.saveAppVersion(BuildConfig.VERSION_NAME)
    }
 */

    private fun removeServers(
        serverList: ArrayList<ServerConfig>,
        config: ServerConfig,
    ): ArrayList<ServerConfig> {
        serverList.remove(config)
        return serverList
    }

    private fun updateServers(
        serverList: ArrayList<ServerConfig>,
        newServer: ServerConfig,
    ): ArrayList<ServerConfig> {
        val serversMap = HashMap<String?, ServerConfig>()
        serverList.forEach {
            serversMap[it.name] = it
        }
        serversMap[newServer.name] = newServer
        return ArrayList(serversMap.values)
    }

    private fun updateOldServers(
        oldList: ArrayList<ServerConfig>,
        newList: ArrayList<ServerConfig>,
    ): ArrayList<ServerConfig> {
        val serversMap = HashMap<String?, ServerConfig>()
        oldList.forEach {
            serversMap[it.name] = it
        }
        newList.forEach {
            serversMap[it.name] = it
        }
        return ArrayList(serversMap.values)
    }

/*    private fun readServersFromFile(context: Context): ArrayList<ServerConfig> {
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
 */

    private fun copyServersFromFile(context: Context) {
        val `in`: InputStream = context.resources.openRawResource(R.raw.servers_config)
        val out = FileOutputStream(context.filesDir.parent + "/shared_prefs/servers_config.xml")
        val buff = ByteArray(1024)
        var read = 0
        try {
            while (`in`.read(buff).also { read = it } > 0) {
                out.write(buff, 0, read)
            }
        } finally {
            `in`.close()
            out.close()
        }
        preferencesProvider.applyServersFromFile(context)
    }

    fun getServers(): List<ServerConfig> {
        return preferencesProvider
            .getAllServers()
            .map { Gson().fromJson<ServerConfig>(it.value) }
    }

/*
    private fun fetchServerNames() {
        currentServerName = getCurrentServer().name
        servers = getServers()
            .map { ServerMenuItem(it.name) }
            .sortedBy { it.name.toString() }
    }

 */

}
