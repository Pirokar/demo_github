package edna.chatcenter.demo.integrationCode.fragments.launch

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import edna.chatcenter.demo.R
import edna.chatcenter.demo.appCode.business.PreferencesProvider
import edna.chatcenter.demo.appCode.business.ServersProvider
import edna.chatcenter.demo.appCode.models.ServerConfig
import edna.chatcenter.demo.appCode.models.TestData
import edna.chatcenter.demo.appCode.models.UserInfo
import edna.chatcenter.ui.core.config.ChatAuth
import edna.chatcenter.ui.core.config.ChatUser
import edna.chatcenter.ui.core.extensions.jsonStringToMap
import edna.chatcenter.ui.core.models.enums.ChatApiVersion
import edna.chatcenter.ui.core.models.enums.ChatApiVersion.Companion.defaultApiVersionEnum
import edna.chatcenter.ui.core.models.enums.CurrentUiTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.parceler.Parcels

class LaunchViewModel(
    private val preferences: PreferencesProvider,
    private val serversProvider: ServersProvider
) : ViewModel(), DefaultLifecycleObserver {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var _selectedApiVersionLiveData = MutableLiveData(getSelectedApiVersion())
    var selectedApiVersionLiveData: LiveData<String> = _selectedApiVersionLiveData

    private var _selectedUserLiveData = MutableLiveData(getSelectedUser())
    var selectedUserLiveData: LiveData<UserInfo?> = _selectedUserLiveData

    val _preregisterLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val preregisterLiveData: LiveData<Boolean> = _preregisterLiveData

    private var _selectedServerLiveData = MutableLiveData(getSelectedServer())
    var selectedServerConfigLiveData: LiveData<ServerConfig?> = _selectedServerLiveData

    private var _enabledLoginButtonLiveData = MutableLiveData(false)
    var enabledLoginButtonLiveData: LiveData<Boolean> = _enabledLoginButtonLiveData

    private var application: edna.chatcenter.demo.appCode.EdnaChatCenterApplication? = null

    fun provideApplication(application: edna.chatcenter.demo.appCode.EdnaChatCenterApplication?) {
        this.application = application
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        preferences.cleanJsonOnPreferences()
        getSelectedServer()?.let { server ->
            if (server.isAllFieldsFilled()) {
                _selectedServerLiveData.postValue(server)
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        checkUiTheme()
        initPreregisterCheckbox()
    }

    fun click(view: View) {
        val navigationController: NavController =
            (view.context as Activity).findNavController(R.id.nav_host_fragment_content_main)
        when (view.id) {
            R.id.serverButton -> navigationController.navigate(R.id.action_LaunchFragment_to_ServerListFragment)
            R.id.demonstrations -> navigationController.navigate(R.id.action_LaunchFragment_to_DemonstrationsListFragment)
            R.id.userButton -> navigationController.navigate(R.id.action_LaunchFragment_to_UserListFragment)
            R.id.login -> { login(navigationController) }
        }
    }

    fun callInitUser(user: UserInfo) {
        if (isPreregisterEnabled) {
            application?.chatCenterUI?.forceAuthorize(
                ChatUser(user.userId!!, data = user.userData?.jsonStringToMap()),
                ChatAuth(user.authorizationHeader, user.xAuthSchemaHeader, signature = user.signature)
            )
        } else {
            application?.chatCenterUI?.authorize(
                ChatUser(user.userId!!, data = user.userData?.jsonStringToMap()),
                ChatAuth(
                    user.authorizationHeader,
                    user.xAuthSchemaHeader,
                    signature = user.signature
                )
            )
        }
    }

    private fun login(navigationController: NavController) {
        if (application?.chatCenterUI == null) {
            return
        }

        val serverConfig = _selectedServerLiveData.value
        val user = _selectedUserLiveData.value
        val isUserHasRequiredFields = user?.userId != null

        if (serverConfig != null && isUserHasRequiredFields) {
            var apiVersion: ChatApiVersion? = _selectedApiVersionLiveData.value?.let {
                ChatApiVersion.createApiVersionEnum(it)
            }
            if (apiVersion == null) {
                apiVersion = ChatApiVersion.defaultApiVersionEnum
            }
            application?.initChatCenterUI(serverConfig, apiVersion)
            if (user != null && !isPreregisterEnabled) callInitUser(user)
            navigationController.navigate(R.id.action_LaunchFragment_to_ChatAppFragment)
        }
    }

    private fun applyCurrentUiTheme(currentUiTheme: CurrentUiTheme) {
        coroutineScope.launch(Dispatchers.Main) {
            when (currentUiTheme) {
                CurrentUiTheme.SYSTEM -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                CurrentUiTheme.LIGHT -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                CurrentUiTheme.DARK -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
            }
        }
    }

    internal fun checkUiTheme() {
        if (application?.chatCenterUI != null) {
            applyCurrentUiTheme(CurrentUiTheme.SYSTEM)
        }
    }

    private fun initPreregisterCheckbox() {
        _preregisterLiveData.postValue(isPreregisterEnabled)
    }

    fun callFragmentResultListener(key: String, bundle: Bundle) {
        if (key == LaunchFragment.SELECTED_USER_KEY && bundle.containsKey(LaunchFragment.SELECTED_USER_KEY)) {
            val user: UserInfo? = if (Build.VERSION.SDK_INT >= 33) {
                Parcels.unwrap(bundle.getParcelable(LaunchFragment.SELECTED_USER_KEY, Parcelable::class.java))
            } else {
                Parcels.unwrap(bundle.getParcelable(LaunchFragment.SELECTED_USER_KEY))
            }
            if (user != null && user.isAllFieldsFilled()) {
                _selectedUserLiveData.postValue(user)
                preferences.saveSelectedUser(user)
            }
        }
        if (key == LaunchFragment.SELECTED_SERVER_CONFIG_KEY && bundle.containsKey(LaunchFragment.SELECTED_SERVER_CONFIG_KEY)) {
            var server: ServerConfig? = if (Build.VERSION.SDK_INT >= 33) {
                Parcels.unwrap(bundle.getParcelable(LaunchFragment.SELECTED_SERVER_CONFIG_KEY, Parcelable::class.java))
            } else {
                Parcels.unwrap(bundle.getParcelable(LaunchFragment.SELECTED_SERVER_CONFIG_KEY))
            }
            if (server == null || !server.isAllFieldsFilled()) {
                server = serversProvider.getSelectedServer()
            }
            if (server != null && server.isAllFieldsFilled()) {
                _selectedServerLiveData.postValue(server)
                serversProvider.saveSelectedServer(server)
            }
        }
    }

    fun subscribeForData(lifecycleOwner: LifecycleOwner) {
        selectedUserLiveData.observe(lifecycleOwner) {
            _enabledLoginButtonLiveData.postValue(it?.isAllFieldsFilled())
        }
    }

    fun onPreregisterCheckedChange(isChecked: Boolean) {
        isPreregisterEnabled = isChecked
    }

    internal fun setSelectedApiVersion(apiVersion: String?) {
        if (!apiVersion.isNullOrBlank()) {
            preferences.saveSelectedApiVersion(apiVersion)
            _selectedApiVersionLiveData.postValue(apiVersion)
        }
    }

    private fun getSelectedApiVersion(): String {
        val apiVersion = preferences.getSelectedApiVersion()
        return if (apiVersion.isNullOrBlank()) {
            defaultApiVersionEnum.toString()
        } else {
            apiVersion
        }
    }

    private fun getSelectedUser(): UserInfo? {
        val testData = edna.chatcenter.demo.BuildConfig.TEST_DATA.get() as? String
        return if (testData.isNullOrEmpty()) {
            val user = preferences.getSelectedUser()
            preferences.getAllUserList().forEach {
                if (it.userId == user?.userId) {
                    return user
                }
            }
            UserInfo()
        } else if (testData.isNotEmpty()) {
            TestData.fromJson(testData).userInfo
        } else {
            null
        }
    }

    private fun getSelectedServer(): ServerConfig? {
        val testData = edna.chatcenter.demo.BuildConfig.TEST_DATA.get() as? String
        return if (testData.isNullOrEmpty()) {
            preferences.getSelectedServer()?.let { server ->
                preferences.getAllServers().forEach {
                    if (server.name == it.name) {
                        return ServerConfig(
                            it.name,
                            it.appMarker,
                            it.threadsGateProviderUid,
                            it.datastoreUrl,
                            it.serverBaseUrl,
                            it.threadsGateUrl,
                            it.isFromApp,
                            it.isShowMenu,
                            it.filesAndMediaMenuItemEnabled,
                            it.trustedSSLCertificates,
                            it.allowUntrustedSSLCertificate,
                            it.isInputEnabled
                        )
                    }
                }
            }
            if (preferences.getAllServers().size > 0) {
                preferences.getAllServers()[0]
            } else {
                null
            }
        } else if (testData.isNotEmpty()) {
            TestData.fromJson(testData).serverConfig
        } else {
            null
        }
    }

    companion object {
        var isPreregisterEnabled = false
    }
}
