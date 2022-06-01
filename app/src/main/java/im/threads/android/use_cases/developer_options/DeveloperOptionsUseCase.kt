package im.threads.android.use_cases.developer_options

import im.threads.android.data.ServerConfig

interface DeveloperOptionsUseCase {
    val mobile1Config: ServerConfig
    val mobile2Config: ServerConfig
    val mobile3Config: ServerConfig
    val mobile4Config: ServerConfig
    val amurtigerConfig: ServerConfig
    val beta3Config: ServerConfig
    val gpbConfig: ServerConfig
    val prodConfig: ServerConfig

    fun isServerNotSet(): Boolean
    fun makeDefaultInit()
    fun getCurrentServer(): ServerConfig
    fun setCurrentServer(serverName: String)
    fun getServers(): List<ServerConfig>
    fun addServer(serverConfig: ServerConfig)
}
