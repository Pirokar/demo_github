package im.threads.android.use_cases.developer_options

import androidx.appcompat.app.AppCompatActivity
import im.threads.android.data.ServerConfig

interface ServersSelectionUseCase {
    fun configureDebugMenu()
    fun isServerNotSet(): Boolean
    fun makeDefaultInit()
    fun setServerAsChanged()
    fun getCurrentServer(): ServerConfig
    fun setCurrentServer(serverName: String)
    fun getServers(): List<ServerConfig>
    fun addServer(serverConfig: ServerConfig)
    fun addUiDependedModulesToDebugMenu(activity: AppCompatActivity)
}
