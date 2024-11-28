package edna.chatcenter.demo.appCode.fragments.log

import android.app.Activity
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import edna.chatcenter.demo.R
import edna.chatcenter.demo.appCode.models.LogModel
import edna.chatcenter.ui.core.logger.ChatLogLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

class LogViewModel : ViewModel(), DefaultLifecycleObserver {

    private var _selectedLogLevelLiveData = MutableLiveData(ChatLogLevel.VERBOSE)
    var selectedLogLevelLiveData: MutableLiveData<ChatLogLevel> = _selectedLogLevelLiveData
    var logsLiveData = MutableLiveData<MutableList<LogModel>>()

    fun onLogViewResume() {
        filterAndShow(selectedLogLevelLiveData.value ?: ChatLogLevel.VERBOSE)
    }

    fun click(view: View) {
        val navigationController: NavController =
            (view.context as Activity).findNavController(R.id.nav_host_fragment_content_main)
        when (view.id) {
            R.id.backButton -> {
                navigationController.navigate(R.id.action_ServersFragment_to_LaunchFragment)
            }
            R.id.addServer -> {
                navigationController.navigate(R.id.action_ServerListFragment_to_AddServerFragment)
            }
        }
    }

    internal fun setLogLevel(logLevel: ChatLogLevel) {
        _selectedLogLevelLiveData.postValue(logLevel)
    }

    @Synchronized
    internal fun filterAndShow(logLevel: ChatLogLevel) {
        viewModelScope.launch(Dispatchers.IO) {
            val newList = logsData.filter {
                it.logLevel.value >= logLevel.value
            }
            logsLiveData.postValue(newList.toMutableList())
        }
    }

    fun clearLog() {
        logsData.clear()
    }

    companion object {
        var logsData = CopyOnWriteArrayList<LogModel>()
    }
}
