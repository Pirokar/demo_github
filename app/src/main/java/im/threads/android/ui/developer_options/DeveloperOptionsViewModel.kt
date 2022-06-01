package im.threads.android.ui.developer_options

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent
import im.threads.android.data.ServerName
import im.threads.android.use_cases.developer_options.DeveloperOptionsUseCase
import kotlin.math.abs
import kotlin.random.Random

class DeveloperOptionsViewModel(private val devOptions: DeveloperOptionsUseCase) : ViewModel() {
    val serverNamesLiveData: LiveData<List<ServerName>> get() = _serverNamesLiveData
    private val _serverNamesLiveData = MutableLiveData<List<ServerName>>()
    val serverChangedLiveData = LiveEvent<Boolean>()
    val restartAppLiveData = LiveEvent<Boolean>()
    private val stringIds = HashMap<Int, String>()

    fun fetchServerNames() {
        val currentServerName = devOptions.getCurrentServer().name
        val servers = devOptions
            .getServers()
            .map { ServerName(it.name, it.name == currentServerName) }
            .sortedBy { it.name }
        _serverNamesLiveData.value = servers
    }

    fun onCheckedChange(checkedId: Int) {
        getStringForId(checkedId)?.let { devOptions.setCurrentServer(it) }
        serverChangedLiveData.value = true
    }

    fun onActivateServerClicked() {
        devOptions.setServerAsChanged()
        restartAppLiveData.value = true
    }

    fun getIdForString(string: String): Int {
        val id = abs((0..Int.MAX_VALUE).random())
        stringIds[id] = string

        return id
    }

    private fun getStringForId(id: Int): String? = stringIds[id]
}
