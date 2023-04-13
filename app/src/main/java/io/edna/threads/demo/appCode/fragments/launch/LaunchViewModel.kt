package io.edna.threads.demo.appCode.fragments.launch

import android.app.Activity
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import im.threads.business.models.enums.CurrentUiTheme
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.PreferencesProvider
import io.edna.threads.demo.appCode.business.SingleLiveEvent
import io.edna.threads.demo.appCode.business.UiThemeProvider
import io.edna.threads.demo.appCode.business.VolatileLiveData
import io.edna.threads.demo.appCode.models.UiTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LaunchViewModel(
    private val preferencesProvider: PreferencesProvider,
    private val uiThemeProvider: UiThemeProvider
) : ViewModel(), DefaultLifecycleObserver {
    val selectServerActionLiveData: SingleLiveEvent<String> = SingleLiveEvent()
    val selectUserActionLiveData: SingleLiveEvent<String> = SingleLiveEvent()
    val currentUiThemeLiveData: MutableLiveData<UiTheme> = MutableLiveData()
    val themeSelectorLiveData: VolatileLiveData<CurrentUiTheme> = VolatileLiveData()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

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
            R.id.serverButton -> navigationController.navigate(R.id.action_LaunchFragment_to_ServersFragment)
            R.id.userButton -> navigationController.navigate(R.id.action_LaunchFragment_to_ServersFragment)
            R.id.demonstrations -> navigationController.navigate(R.id.action_LaunchFragment_to_DemonstrationsListFragment)
            R.id.uiTheme -> themeSelectorLiveData.postValue(ThreadsLib.getInstance().currentUiTheme)
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
}
