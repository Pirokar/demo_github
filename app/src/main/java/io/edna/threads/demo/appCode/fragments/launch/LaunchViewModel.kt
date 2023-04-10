package io.edna.threads.demo.appCode.fragments.launch

import android.app.Activity
import android.view.View
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.PreferencesProvider
import io.edna.threads.demo.models.ServerConfig

class LaunchViewModel(private val preferencesProvider: PreferencesProvider) :
    ViewModel(),
    DefaultLifecycleObserver {
    private var _selectedServerLiveData = MutableLiveData(ServerConfig())
    var selectedServerConfigLiveData: LiveData<ServerConfig> = _selectedServerLiveData

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        preferencesProvider.cleanJsonOnPreferences()
    }

    fun click(view: View) {
        val navigationController: NavController =
            (view.context as Activity).findNavController(R.id.nav_host_fragment_content_main)
        when (view.id) {
            R.id.serverButton -> navigationController.navigate(R.id.action_LaunchFragment_to_ServersFragment)
            R.id.userButton -> navigationController.navigate(R.id.action_LaunchFragment_to_ServersFragment)
            R.id.demonstrations -> navigationController.navigate(R.id.action_LaunchFragment_to_DemonstrationsListFragment)
            R.id.settings -> Toast.makeText(
                view.context,
                view.context.getString(R.string.functional_not_support),
                Toast.LENGTH_LONG
            ).show()
            R.id.login -> {}
        }
    }

    fun setupServerConfig(config: ServerConfig) {
        _selectedServerLiveData.postValue(config)
    }
}
