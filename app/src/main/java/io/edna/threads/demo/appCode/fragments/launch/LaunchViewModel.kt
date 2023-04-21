package io.edna.threads.demo.appCode.fragments.launch

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
import im.threads.business.models.enums.CurrentUiTheme
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.PreferencesProvider
import io.edna.threads.demo.appCode.business.UiThemeProvider
import io.edna.threads.demo.appCode.business.VolatileLiveData
import io.edna.threads.demo.appCode.models.ServerConfig
import io.edna.threads.demo.appCode.models.UiTheme
import io.edna.threads.demo.appCode.models.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.parceler.Parcels

class LaunchViewModel(
    private val preferencesProvider: PreferencesProvider,
    private val uiThemeProvider: UiThemeProvider
) : ViewModel(), DefaultLifecycleObserver {
    val currentUiThemeLiveData: MutableLiveData<UiTheme> = MutableLiveData()
    val themeSelectorLiveData: VolatileLiveData<CurrentUiTheme> = VolatileLiveData()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var _selectedUserLiveData = MutableLiveData(preferencesProvider.getSelectedUser())
    var selectedUserLiveData: LiveData<UserInfo> = _selectedUserLiveData

    private var _selectedServerLiveData = MutableLiveData(preferencesProvider.getSelectedServer())
    var selectedServerConfigLiveData: LiveData<ServerConfig> = _selectedServerLiveData

    private var _enabledLoginButtonLiveData = MutableLiveData(false)
    var enabledLoginButtonLiveData: LiveData<Boolean> = _enabledLoginButtonLiveData

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        preferencesProvider.cleanJsonOnPreferences()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        checkUiTheme()
    }

    fun click(view: View) {
        val navigationController: NavController =
            (view.context as Activity).findNavController(R.id.nav_host_fragment_content_main)
        when (view.id) {
            R.id.serverButton -> navigationController.navigate(R.id.action_LaunchFragment_to_ServerListFragment)
            R.id.demonstrations -> navigationController.navigate(R.id.action_LaunchFragment_to_DemonstrationsListFragment)
            R.id.uiTheme -> themeSelectorLiveData.postValue(ThreadsLib.getInstance().currentUiTheme)
            R.id.userButton -> navigationController.navigate(R.id.action_LaunchFragment_to_UserListFragment)
            R.id.login -> {}
        }
    }

    fun saveUserSelectedUiTheme(theme: CurrentUiTheme) {
        coroutineScope.launch {
            ThreadsLib.getInstance().currentUiTheme = theme
            currentUiThemeLiveData.postValue(getCurrentUiTheme(theme))
            applyCurrentUiTheme(theme)
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

    private fun checkUiTheme() {
        val uiTheme = ThreadsLib.getInstance().currentUiTheme
        applyCurrentUiTheme(uiTheme)
        currentUiThemeLiveData.value = getCurrentUiTheme(uiTheme)
    }

    private fun getCurrentUiTheme(currentUiTheme: CurrentUiTheme): UiTheme {
        return when (currentUiTheme) {
            CurrentUiTheme.LIGHT -> UiTheme.LIGHT
            CurrentUiTheme.DARK -> UiTheme.DARK
            CurrentUiTheme.SYSTEM -> {
                if (uiThemeProvider.isDarkThemeOn()) {
                    UiTheme.DARK
                } else {
                    UiTheme.LIGHT
                }
            }
        }
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
                preferencesProvider.saveSelectedUser(user)
            }
        }
        if (key == LaunchFragment.SELECTED_SERVER_CONFIG_KEY && bundle.containsKey(LaunchFragment.SELECTED_SERVER_CONFIG_KEY)) {
            val server: ServerConfig? = if (Build.VERSION.SDK_INT >= 33) {
                Parcels.unwrap(bundle.getParcelable(LaunchFragment.SELECTED_SERVER_CONFIG_KEY, Parcelable::class.java))
            } else {
                Parcels.unwrap(bundle.getParcelable(LaunchFragment.SELECTED_SERVER_CONFIG_KEY))
            }
            if (server != null && server.isAllFieldsFilled()) {
                _selectedServerLiveData.postValue(server)
                preferencesProvider.saveSelectedServer(server)
            }
        }
    }

    fun subscribeForData(lifecycleOwner: LifecycleOwner) {
        selectedUserLiveData.observe(lifecycleOwner) {
            _enabledLoginButtonLiveData.postValue(it.isAllFieldsFilled())
        }
    }
}
