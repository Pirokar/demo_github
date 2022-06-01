package im.threads.android.use_cases.developer_options

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import im.threads.android.data.ServerConfig
import im.threads.android.data.TransportConfig
import im.threads.android.utils.PrefUtilsApp
import im.threads.android.utils.fromJson
import im.threads.android.utils.toJson

class DeveloperOptionsInteractor(private val context: Context) : DeveloperOptionsUseCase {
    private val TAG = "DeveloperOptions"

    override val mobile1Config = ServerConfig(
        "Mobile 1",
        "http://datastore.mobile1.chc.dte/",
        "http://arm.mobile1.chc.dte",
        "http://tg.mobile1.chc.dte/socket",
        "MOBILE1_93jLrvripZeDXSKJzdRfEu9QpMMvIe5LKKHQl",
        false
    )

    override val mobile2Config = ServerConfig(
        "Mobile 2",
        "http://datastore.mobile2.chc.dte/",
        "http://arm.mobile2.chc.dte",
        "http://tg.mobile2.chc.dte/socket",
        "MOBILE2_oYrHwZ9QhTihb2d8U3I17dBHy1NB9vA9XVkM",
        false
    )

    override val mobile3Config = ServerConfig(
        "Mobile 3",
        "http://datastore.mobile3.chc.dte/",
        "http://arm.mobile3.chc.dte",
        "http://tg.mobile3.chc.dte/socket",
        "MOBILE3_MMvIe5LKKHQlepr8vripZeDXSKJzdRfEu9Qp",
        false
    )

    override val mobile4Config = ServerConfig(
        "Mobile 4",
        "https://mobile4.dev.flex.mfms.ru",
        "https://mobile4.dev.flex.mfms.ru",
        "wss://mobile4.dev.flex.mfms.ru/gate/socket",
        "MOBILE4_HwZ9QhTihb2d8U3I17dBHy1NB9vA9XVkMz65",
        false
    )

    override val amurtigerConfig = ServerConfig(
        "Amurtiger",
        "https://amurtiger.edna.io/",
        "https://amurtiger.edna.io/",
        "wss://amurtiger.edna.io/socket",
        "PH5ucnVkYXMtbmV3LTE1MzE5MTQ2NTk4MDItZ2VuZXJhdGVkV2l0aFVJfj4",
        true
    )

    override val beta3Config = ServerConfig(
        "Beta 3",
        "https://arm.beta3.chc.dte/",
        "https://arm.beta3.chc.dte",
        "wss://arm.beta3.chc.dte/gate/socket",
        "KtfvH538KBfjoMMY9Q9ha65CtWeMshQb6nBPhAY12SMH8",
        true
    )

    override val gpbConfig = ServerConfig(
        "GPB",
        "http://open-ig.gpb-test.chc.dte/",
        "http://open-ig.gpb-test.chc.dte",
        "ws://open-ig.gpb-test.chc.dte/socket",
        "GPB-TEST_iT6VrvripZeDCCVJzdRfEu9QpMMvIe5L5KcEM",
        true,
        newChatCenterApi = true
    )

    override val prodConfig = ServerConfig(
        "PROD",
        "https://beta-prod.edna.ru/",
        "https://beta-prod.edna.ru",
        "wss://beta-prod.edna.ru/socket",
        "YmV0YS1wcm9kLmVkbmEucnU7O2FuZHJvaWQ7OzIwMjIwMzI4",
        true
    )

    override fun isServerNotSet() = getLatestServer() == null

    override fun makeDefaultInit() {
        addExistingServers()
        setCurrentServer(mobile1Config.name)
        PrefUtilsApp.saveTransportConfig(
            context,
            TransportConfig(
                mobile1Config.serverBaseUrl,
                threadsGateUrl = mobile1Config.threadsGateUrl,
                threadsGateProviderUid = mobile1Config.threadsGateProviderUid
            )
        )
    }

    override fun setServerAsChanged() {
        PrefUtilsApp.setIsServerChanged(context, true)
    }

    override fun getCurrentServer() = getLatestServer() ?: mobile1Config

    override fun setCurrentServer(serverName: String) {
        getServers().firstOrNull { it.name == serverName }?.let { serverConfig ->
            PrefUtilsApp.saveTransportConfig(
                context,
                TransportConfig(
                    serverConfig.serverBaseUrl,
                    threadsGateUrl = serverConfig.threadsGateUrl,
                    threadsGateProviderUid = serverConfig.threadsGateProviderUid
                )
            )
            PrefUtilsApp.setCurrentServer(context, serverName)
        } ?: Log.e(TAG, "Cannot set server!")
    }

    override fun getServers(): List<ServerConfig> {
        return PrefUtilsApp
            .getAllServers(context)
            .map { Gson().fromJson<ServerConfig>(it.value) }
    }

    override fun addServer(serverConfig: ServerConfig) {
        val map = hashMapOf(Pair(serverConfig.name, serverConfig.toJson()))
        PrefUtilsApp.addServers(context, map)
    }

    private fun addExistingServers() {
        val hashMap = hashMapOf(
            Pair(mobile1Config.name, mobile1Config.toJson()),
            Pair(mobile2Config.name, mobile2Config.toJson()),
            Pair(mobile3Config.name, mobile3Config.toJson()),
            Pair(mobile4Config.name, mobile4Config.toJson()),
            Pair(amurtigerConfig.name, amurtigerConfig.toJson()),
            Pair(beta3Config.name, beta3Config.toJson()),
            Pair(gpbConfig.name, gpbConfig.toJson()),
            Pair(prodConfig.name, prodConfig.toJson())
        )
        PrefUtilsApp.addServers(context, hashMap)
    }

    private fun getLatestServer(): ServerConfig? {
        val preferencesMap = PrefUtilsApp.getAllServers(context)
        return preferencesMap[PrefUtilsApp.getCurrentServer(context)]?.let {
            Gson().fromJson<ServerConfig>(it)
        }
    }
}
