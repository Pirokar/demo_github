package edna.chatcenter.demo.appCode.fragments.log

import android.app.Activity
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import edna.chatcenter.demo.R
import edna.chatcenter.demo.appCode.models.LogModel
import edna.chatcenter.ui.core.logger.ChatLogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogViewModel : ViewModel(), DefaultLifecycleObserver {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var _logLiveData = logsFlow
    var logLiveData: MutableLiveData<ArrayList<LogModel>> = _logLiveData

    private var _logListLiveData = MutableLiveData(ArrayList<LogModel>())
    var logListLiveData: MutableLiveData<ArrayList<LogModel>> = _logListLiveData

    private var _selectedLogLevelLiveData = MutableLiveData(ChatLogLevel.DEBUG)
    var selectedLogLevelLiveData: MutableLiveData<ChatLogLevel> = _selectedLogLevelLiveData

    fun click(view: View) {
        val navigationController: NavController =
            (view.context as Activity).findNavController(R.id.nav_host_fragment_content_main)
        when (view.id) {
            R.id.backButton -> navigationController.navigate(R.id.action_ServersFragment_to_LaunchFragment)
            R.id.addServer -> {
                navigationController.navigate(R.id.action_ServerListFragment_to_AddServerFragment)
            }
        }
    }

    @Synchronized
    fun addItems(items: ArrayList<LogModel>) {
        coroutineScope.launch {
            val arr = _logListLiveData.value
            items.forEach { logModel ->
                selectedLogLevelLiveData.value?.let {
                    if (logModel.logLevel >= it) {
                        arr?.add(logModel)
                    }
                }
            }
            withContext(Dispatchers.Main) { _logListLiveData.postValue(arr) }
        }
    }

    internal fun setLogLevel(logLevel: ChatLogLevel) {
        _selectedLogLevelLiveData.postValue(logLevel)
    }

    internal fun filter(logLevel: ChatLogLevel) {
        coroutineScope.launch {
            val list = _logListLiveData.value
            list?.clear()
            logsFlow.value?.forEach { logModel ->
                if (logModel.logLevel.value >= logLevel.value) {
                    list?.add(logModel)
                }
            }
            withContext(Dispatchers.Main) { _logListLiveData.postValue(list) }
        }
    }

    fun clearLog() {
        logsFlow.value?.clear()
        _logLiveData.value?.clear()
        _logListLiveData.value?.clear()
    }

    companion object {
        var logsFlow = MutableLiveData<ArrayList<LogModel>>(ArrayList())
    }
}
