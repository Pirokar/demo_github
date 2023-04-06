package io.edna.threads.demo.ui.fragments.server

import android.app.Activity
import android.content.Context
import android.util.Log
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
import io.edna.threads.demo.models.ServerConfig
import io.edna.threads.demo.utils.PrefUtilsApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream

class ServersViewModel : ViewModel(), DefaultLifecycleObserver {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var _serverListLiveData = MutableLiveData(ArrayList<ServerConfig>())
    var serverConfigLiveData: LiveData<ArrayList<ServerConfig>> = _serverListLiveData

    fun click(view: View) {
        val navigationController: NavController =
            (view.context as Activity).findNavController(R.id.nav_host_fragment_content_main)
        when (view.id) {
            R.id.backButton -> navigationController.navigate(R.id.action_ServersFragment_to_LaunchFragment)
            R.id.addServer -> {
                navigationController.navigate(R.id.action_ServersFragment_to_AddServerFragment)
            }
        }
    }

    fun backToLaunchScreen(context: Context?) {
        context?.let {
            val navigationController: NavController =
                (it as Activity).findNavController(R.id.nav_host_fragment_content_main)
            navigationController.navigate(R.id.action_ServersFragment_to_LaunchFragment)
        }
    }

    fun addConfig(context: Context?, config: ServerConfig) {
        context?.let {
            coroutineScope.launch {
                addServer(it, config)
            }
        }
    }

    fun copyServersFromFileIfNeed(context: Context?) {
        context?.let {
            Log.e(
                "Tag",
                "copyServersFromFileIfNeed()   " + PrefUtilsApp.getSavedAppVersion(it) + " ==? " + BuildConfig.VERSION_NAME
            )
            if (PrefUtilsApp.getSavedAppVersion(it) != BuildConfig.VERSION_NAME) {
                coroutineScope.launch {
                    copyServersFromFile(it)
                }
            } else {
                _serverListLiveData.postValue(PrefUtilsApp.getAllServers(it))
            }
        }
    }

    private suspend fun addServer(context: Context, config: ServerConfig) =
        withContext(Dispatchers.IO) {
            val servers = PrefUtilsApp.getAllServers(context)
            _serverListLiveData.postValue(updateServers(servers, config))
            PrefUtilsApp.saveAppVersion(context, BuildConfig.VERSION_NAME)
            serverConfigLiveData.value?.let {
                Log.e("Tag", "addServer()   " + it.toString())
                PrefUtilsApp.saveServers(context, it)
            }
        }

    private suspend fun copyServersFromFile(context: Context) = withContext(Dispatchers.IO) {
        val newServers = readServersFromFile(context)
        val oldServers = PrefUtilsApp.getAllServers(context)
        _serverListLiveData.postValue(updateOldServers(oldServers, newServers))
        PrefUtilsApp.saveAppVersion(context, BuildConfig.VERSION_NAME)
        serverConfigLiveData.value?.let {
            Log.e("Tag", "copyServersFromFile()   " + it.toString())
            PrefUtilsApp.saveServers(context, it)
        }
    }

    private fun updateServers(
        serverList: ArrayList<ServerConfig>,
        newServer: ServerConfig
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
        newList: ArrayList<ServerConfig>
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

    private fun readServersFromFile(context: Context): ArrayList<ServerConfig> {
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
}
