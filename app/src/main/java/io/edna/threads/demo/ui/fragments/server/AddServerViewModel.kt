package io.edna.threads.demo.ui.fragments.server

import android.app.Activity
import android.text.Editable
import android.view.View
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import io.edna.threads.demo.R
import io.edna.threads.demo.models.ServerConfig
import io.edna.threads.demo.utils.AfterTextChangedTextWatcher

class AddServerViewModel : ViewModel(), DefaultLifecycleObserver, Observable {

    var srcServerConfigLiveData = MutableLiveData<ServerConfig>(null)
    var finalServerConfigLiveData = MutableLiveData<ServerConfig>(null)
    private var _serverConfigLiveData = MutableLiveData(ServerConfig())
    var serverConfigLiveData: LiveData<ServerConfig> = _serverConfigLiveData

    private var _enabledSaveButtonLiveData = MutableLiveData(false)
    var enabledSaveButtonLiveData: LiveData<Boolean> = _enabledSaveButtonLiveData

    fun serSrcConfig(config: ServerConfig?) {
        config?.let {
            srcServerConfigLiveData.postValue(it)
            _serverConfigLiveData.postValue(it)
        }
    }

    fun click(view: View) {
        val navigationController: NavController =
            (view.context as Activity).findNavController(R.id.nav_host_fragment_content_main)
        when (view.id) {
            R.id.backButton -> navigationController.navigate(R.id.action_AddServerFragment_back_to_ServersFragment)
            R.id.okButton -> {
                finalServerConfigLiveData.postValue(serverConfigLiveData.value)
                navigationController.navigate(R.id.action_AddServerFragment_to_ServersFragment)
            }
        }
    }

    fun subscribeForData(lifecycleOwner: LifecycleOwner) {
        serverConfigLiveData.observe(lifecycleOwner) {
            if (srcServerConfigLiveData.value == null) {
                _enabledSaveButtonLiveData.postValue(it.isAllFieldsFilled())
            } else {
                if (it.isAllFieldsFilled()) {
                    _enabledSaveButtonLiveData.postValue(!it.equals(srcServerConfigLiveData.value))
                } else {
                    _enabledSaveButtonLiveData.postValue(it.isAllFieldsFilled())
                }
            }
        }
    }

    @get:Bindable
    val nameTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            s?.let {
                if (_serverConfigLiveData.value?.name != it.toString()) {
                    serverConfigLiveData.value?.name = it.toString()
                    _serverConfigLiveData.postValue(serverConfigLiveData.value)
                }
            }
        }
    }

    @get:Bindable
    val providerIdTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            s?.let {
                if (serverConfigLiveData.value?.threadsGateProviderUid != it.toString()) {
                    serverConfigLiveData.value?.threadsGateProviderUid = it.toString()
                    _serverConfigLiveData.postValue(serverConfigLiveData.value)
                }
            }
        }
    }

    @get:Bindable
    val baseUrlTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            s?.let {
                if (serverConfigLiveData.value?.serverBaseUrl != it.toString()) {
                    serverConfigLiveData.value?.serverBaseUrl = it.toString()
                    _serverConfigLiveData.postValue(serverConfigLiveData.value)
                }
            }
        }
    }

    @get:Bindable
    val datastoreUrlTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            s?.let {
                if (serverConfigLiveData.value?.datastoreUrl != it.toString()) {
                    serverConfigLiveData.value?.datastoreUrl = it.toString()
                    _serverConfigLiveData.postValue(serverConfigLiveData.value)
                }
            }
        }
    }

    @get:Bindable
    val threadsGateUrlTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            s?.let {
                if (serverConfigLiveData.value?.threadsGateUrl != it.toString()) {
                    serverConfigLiveData.value?.threadsGateUrl = it.toString()
                    _serverConfigLiveData.postValue(serverConfigLiveData.value)
                }
            }
        }
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {}
    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {}
}
