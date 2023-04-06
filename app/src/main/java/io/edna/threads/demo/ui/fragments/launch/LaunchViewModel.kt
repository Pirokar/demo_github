package io.edna.threads.demo.ui.fragments.launch

import android.app.Activity
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import io.edna.threads.demo.R
import io.edna.threads.demo.models.ServerConfig
import io.edna.threads.demo.utils.SingleLiveEvent

class LaunchViewModel : ViewModel() {

    private var _selectedServerLiveData = MutableLiveData(ServerConfig())
    var selectedServerConfigLiveData: LiveData<ServerConfig> = _selectedServerLiveData

    val selectUserAction: SingleLiveEvent<String> = SingleLiveEvent()

    fun click(view: View) {
        val navigationController: NavController =
            (view.context as Activity).findNavController(R.id.nav_host_fragment_content_main)
        when (view.id) {
            R.id.serverButton -> navigationController.navigate(R.id.action_LaunchFragment_to_ServersFragment)
            R.id.userButton -> navigationController.navigate(R.id.action_LaunchFragment_to_ServersFragment)
            R.id.demonstrations -> Toast.makeText(
                view.context,
                view.context.getString(R.string.functional_not_support),
                Toast.LENGTH_LONG
            ).show()
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
