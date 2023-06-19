package im.threads.android.useCases.developerOptions

import androidx.appcompat.app.AppCompatActivity
import im.threads.android.data.ServerConfig

interface DebugMenuUseCase {
    fun configureDebugMenu()
    fun isServerNotSet(): Boolean
    fun initServer()
    fun setServerAsChanged()
    fun getCurrentServer(): ServerConfig
    fun setCurrentServer(serverName: String)
    fun getServers(): List<ServerConfig>
    fun addServer(serverConfig: ServerConfig)
    fun addUiDependedModulesToDebugMenu(activity: AppCompatActivity)
}
