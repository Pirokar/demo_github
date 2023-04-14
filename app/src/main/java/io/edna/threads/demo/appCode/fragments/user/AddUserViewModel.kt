package io.edna.threads.demo.appCode.fragments.user

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
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
import io.edna.threads.demo.appCode.business.AfterTextChangedTextWatcher
import io.edna.threads.demo.appCode.models.UserInfo
import org.parceler.Parcels

class AddUserViewModel : ViewModel(), DefaultLifecycleObserver, Observable {

    var finalUserLiveData = MutableLiveData<UserInfo>(null)
    private var srcUserLiveData = MutableLiveData<UserInfo?>(null)
    private var _userLiveData = MutableLiveData(UserInfo())
    var userLiveData: LiveData<UserInfo> = _userLiveData

    private var _enabledSaveButtonLiveData = MutableLiveData(false)
    var enabledSaveButtonLiveData: LiveData<Boolean> = _enabledSaveButtonLiveData

    fun initData(arguments: Bundle?) {
        arguments?.let { bundle ->
            if (bundle.containsKey(UserListFragment.USER_KEY)) {
                val user: UserInfo? = if (Build.VERSION.SDK_INT >= 33) {
                    Parcels.unwrap(bundle.getParcelable(UserListFragment.USER_KEY, Parcelable::class.java))
                } else {
                    Parcels.unwrap(bundle.getParcelable(UserListFragment.USER_KEY))
                }
                if (user != null) {
                    srcUserLiveData.postValue(user)
                    _userLiveData.postValue(user.clone())
                }
            }
        }
    }

    fun click(view: View) {
        val navigationController: NavController =
            (view.context as Activity).findNavController(R.id.nav_host_fragment_content_main)
        when (view.id) {
            R.id.backButton -> navigationController.navigate(R.id.action_AddUserFragment_to_UserListFragment)
            R.id.okButton -> {
                finalUserLiveData.postValue(userLiveData.value)
                navigationController.navigate(R.id.action_AddUserFragment_to_UserListFragment)
            }
        }
    }

    fun subscribeForData(lifecycleOwner: LifecycleOwner) {
        userLiveData.observe(lifecycleOwner) {
            if (srcUserLiveData.value == null) {
                _enabledSaveButtonLiveData.postValue(it.isAllFieldsFilled())
            } else {
                if (it.isAllFieldsFilled()) {
                    _enabledSaveButtonLiveData.postValue(!it.equals(srcUserLiveData.value))
                } else {
                    _enabledSaveButtonLiveData.postValue(false)
                }
            }
        }
    }

    @get:Bindable
    val nickNameTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                if (_userLiveData.value?.nickName != s.toString()) {
                    userLiveData.value?.nickName = s.toString()
                    _userLiveData.postValue(userLiveData.value)
                }
            }
        }
    }

    @get:Bindable
    val userIdTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                if (userLiveData.value?.userId != s.toString()) {
                    userLiveData.value?.userId = s.toString()
                    _userLiveData.postValue(userLiveData.value)
                }
            }
        }
    }

    @get:Bindable
    val userDataTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                if (userLiveData.value?.userData != s.toString()) {
                    userLiveData.value?.userData = s.toString()
                    _userLiveData.postValue(userLiveData.value)
                }
            }
        }
    }

    @get:Bindable
    val appMarkerTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                if (userLiveData.value?.appMarker != s.toString()) {
                    userLiveData.value?.appMarker = s.toString()
                    _userLiveData.postValue(userLiveData.value)
                }
            }
        }
    }

    @get:Bindable
    val signatureTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                if (userLiveData.value?.signature != s.toString()) {
                    userLiveData.value?.signature = s.toString()
                    _userLiveData.postValue(userLiveData.value)
                }
            }
        }
    }

    @get:Bindable
    val authorizationHeaderTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                if (userLiveData.value?.authorizationHeader != s.toString()) {
                    userLiveData.value?.authorizationHeader = s.toString()
                    _userLiveData.postValue(userLiveData.value)
                }
            }
        }
    }

    @get:Bindable
    val xAuthSchemaHeaderTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                if (userLiveData.value?.xAuthSchemaHeader != s.toString()) {
                    userLiveData.value?.xAuthSchemaHeader = s.toString()
                    _userLiveData.postValue(userLiveData.value)
                }
            }
        }
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {}
    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {}
}
