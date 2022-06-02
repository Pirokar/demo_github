package im.threads.android.use_cases.developer_options

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.jakewharton.processphoenix.ProcessPhoenix
import com.pandulapeter.beagle.Beagle
import com.pandulapeter.beagle.common.configuration.Appearance
import com.pandulapeter.beagle.common.configuration.Behavior
import com.pandulapeter.beagle.common.configuration.Placement
import com.pandulapeter.beagle.common.configuration.toText
import com.pandulapeter.beagle.common.contracts.BeagleListItemContract
import com.pandulapeter.beagle.logCrash.BeagleCrashLogger
import com.pandulapeter.beagle.logOkHttp.BeagleOkHttpLogger
import com.pandulapeter.beagle.modules.AnimationDurationSwitchModule
import com.pandulapeter.beagle.modules.AppInfoButtonModule
import com.pandulapeter.beagle.modules.BugReportButtonModule
import com.pandulapeter.beagle.modules.DeveloperOptionsButtonModule
import com.pandulapeter.beagle.modules.DeviceInfoModule
import com.pandulapeter.beagle.modules.DividerModule
import com.pandulapeter.beagle.modules.ForceCrashButtonModule
import com.pandulapeter.beagle.modules.HeaderModule
import com.pandulapeter.beagle.modules.KeylineOverlaySwitchModule
import com.pandulapeter.beagle.modules.LifecycleLogListModule
import com.pandulapeter.beagle.modules.LogListModule
import com.pandulapeter.beagle.modules.NetworkLogListModule
import com.pandulapeter.beagle.modules.PaddingModule
import com.pandulapeter.beagle.modules.ScreenCaptureToolboxModule
import com.pandulapeter.beagle.modules.SingleSelectionListModule
import com.pandulapeter.beagle.modules.TextModule
import im.threads.android.BuildConfig
import im.threads.android.R
import im.threads.android.core.ThreadsDemoApplication
import im.threads.android.data.ServerConfig
import im.threads.android.data.TransportConfig
import im.threads.android.ui.EditTransportConfigDialog
import im.threads.android.ui.MainActivity
import im.threads.android.ui.add_server_dialog.AddServerDialog
import im.threads.android.ui.add_server_dialog.AddServerDialogActions
import im.threads.android.utils.PrefUtilsApp
import im.threads.android.utils.fromJson
import im.threads.android.utils.toJson

class ServersSelectionInteractor(private val context: Context) : ServersSelectionUseCase {
    private val TAG = "DeveloperOptions"
    private var isServersListInitialized = false
    private var currentServerName = ""
    private var servers = listOf<ServerMenuItem>()

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

    override fun configureDebugMenu() {
        fetchServerNames()
        Beagle.initialize(
            context as ThreadsDemoApplication,
            appearance = Appearance(
                themeResourceId = R.style.DebugMenuTheme
            ),
            behavior = Behavior(
                bugReportingBehavior = Behavior.BugReportingBehavior(
                    crashLoggers = listOf(BeagleCrashLogger),
                    buildInformation = {
                        listOf(
                            "Version name".toText() to BuildConfig.VERSION_NAME,
                            "Version code".toText() to BuildConfig.VERSION_CODE.toString(),
                            "Application ID".toText() to BuildConfig.APPLICATION_ID
                        )
                    }
                ),
                networkLogBehavior = Behavior.NetworkLogBehavior(
                    networkLoggers = listOf(BeagleOkHttpLogger)
                ),
                getDrawerSize = { _, size ->
                    size - 200
                }
            )
        )
        setModulesToBeagle()
    }

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

    override fun addUiDependedModulesToDebugMenu(activity: AppCompatActivity) {
        val editTransportModule = TextModule(
            getString(R.string.demo_change_current_server),
            TextModule.Type.BUTTON,
            onItemSelected = {
                EditTransportConfigDialog.open(activity)
            }
        )
        val paddingModule = PaddingModule(PaddingModule.Size.MEDIUM)
        val addServerModule = TextModule(
            getString(R.string.demo_add_server),
            TextModule.Type.BUTTON,
            onItemSelected = {
                val onServerAddedAction = object : AddServerDialogActions {
                    override fun onServerAdded() {
                        fetchServerNames()
                        setModulesToBeagle()
                        addUiDependedModulesToDebugMenu(activity)
                    }
                }
                AddServerDialog.open(activity, onServerAddedAction)
            }
        )
        Beagle.add(
            editTransportModule,
            paddingModule,
            addServerModule,
            placement = Placement.Below(SINGLE_SELECTION_MODULE_ID)
        )
    }

    private fun setModulesToBeagle() {
        Beagle.set(
            HeaderModule(
                title = getCurrentServerTitle(),
                subtitle = BuildConfig.APPLICATION_ID,
                text = "${BuildConfig.BUILD_TYPE} v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            ),
            PaddingModule(size = PaddingModule.Size.LARGE),
            BugReportButtonModule(),
            ScreenCaptureToolboxModule(),
            DividerModule(),
            TextModule(getString(R.string.demo_server_selection), TextModule.Type.SECTION_HEADER),
            SingleSelectionListModule(
                id = SINGLE_SELECTION_MODULE_ID,
                title = currentServerName,
                items = servers,
                isExpandedInitially = false,
                isValuePersisted = true,
                initiallySelectedItemId = currentServerName,
                onSelectionChanged = { onServerChanged(it) }
            ),
            DividerModule(),
            TextModule("Logs", TextModule.Type.SECTION_HEADER),
            NetworkLogListModule(),
            LogListModule(maxItemCount = 100),
            LifecycleLogListModule(),
            DividerModule(),
            TextModule("Debug", TextModule.Type.SECTION_HEADER),
            AnimationDurationSwitchModule(),
            KeylineOverlaySwitchModule(),
            DeviceInfoModule(),
            DeveloperOptionsButtonModule(),
            PaddingModule(size = PaddingModule.Size.LARGE),
            AppInfoButtonModule(getString(R.string.about_app).toText()),
            ForceCrashButtonModule(),
        )
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

    private fun onServerChanged(serverMenuItem: ServerMenuItem?) {
        if (isServersListInitialized) {
            serverMenuItem?.let {
                currentServerName = it.name.toString()
                setCurrentServer(it.name.toString())
                Toast.makeText(
                    context,
                    getString(R.string.demo_restart_app_for_server_apply),
                    Toast.LENGTH_SHORT
                ).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    ProcessPhoenix.triggerRebirth(context, Intent(context, MainActivity::class.java))
                }, 2000)
            }
        }
        isServersListInitialized = true
    }

    private fun getLatestServer(): ServerConfig? {
        val preferencesMap = PrefUtilsApp.getAllServers(context)
        return preferencesMap[PrefUtilsApp.getCurrentServer(context)]?.let {
            Gson().fromJson<ServerConfig>(it)
        }
    }

    private fun fetchServerNames() {
        currentServerName = getCurrentServer().name
        servers = getServers()
            .map { ServerMenuItem(it.name) }
            .sortedBy { it.name.toString() }
    }

    private fun getCurrentServerTitle() = "${getString(R.string.demo_server)}: $currentServerName"

    private fun getString(resId: Int) = context.getString(resId)

    data class ServerMenuItem(val name: CharSequence) : BeagleListItemContract {
        override val title = name.toText()
    }

    companion object {
        private const val SINGLE_SELECTION_MODULE_ID = "singleSelectionModuleId"
    }
}
