package io.edna.threads.demo.ui.fragments.user

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import io.edna.threads.demo.R
import io.edna.threads.demo.models.UserInfo
import io.edna.threads.demo.utils.PrefUtilsApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserListViewModel : ViewModel(), DefaultLifecycleObserver {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var _userListLiveData = MutableLiveData(ArrayList<UserInfo>())
    var userListLiveData: LiveData<ArrayList<UserInfo>> = _userListLiveData

    fun click(view: View) {
        val navigationController: NavController =
            (view.context as Activity).findNavController(R.id.nav_host_fragment_content_main)
        when (view.id) {
            R.id.backButton -> navigationController.navigate(R.id.action_UserListFragment_to_LaunchFragment)
            R.id.addUser -> {
                navigationController.navigate(R.id.action_UserListFragment_to_AddUserFragment)
            }
        }
    }

    fun backToLaunchScreen(context: Context?) {
        context?.let {
            val navigationController: NavController =
                (it as Activity).findNavController(R.id.nav_host_fragment_content_main)
            navigationController.navigate(R.id.action_UserListFragment_to_LaunchFragment)
        }
    }

    fun addUser(context: Context?, user: UserInfo) {
        context?.let {
            coroutineScope.launch {
                addUser(it, user)
            }
        }
    }

    fun removeUser(context: Context?, user: UserInfo) {
        context?.let {
            coroutineScope.launch {
                removeUser(it, user)
            }
        }
    }

    fun loadUserList(context: Context?) {
        context?.let {
            _userListLiveData.postValue(PrefUtilsApp.getAllUserList(it))
        }
    }

    private suspend fun removeUser(context: Context, user: UserInfo) =
        withContext(Dispatchers.IO) {
            val srcUserList = PrefUtilsApp.getAllUserList(context)
            val finalUserList = removeUser(srcUserList, user)
            _userListLiveData.postValue(finalUserList)
            PrefUtilsApp.saveUserList(context, finalUserList)
        }

    private suspend fun addUser(context: Context, user: UserInfo) =
        withContext(Dispatchers.IO) {
            val userList = PrefUtilsApp.getAllUserList(context)
            val newUserList = updateUserList(userList, user)
            _userListLiveData.postValue(newUserList)
            PrefUtilsApp.saveUserList(context, newUserList)
        }

    private fun removeUser(
        userList: ArrayList<UserInfo>,
        user: UserInfo,
    ): ArrayList<UserInfo> {
        userList.remove(user)
        return userList
    }

    private fun updateUserList(
        userList: ArrayList<UserInfo>,
        newUser: UserInfo,
    ): ArrayList<UserInfo> {
        val serversMap = HashMap<String?, UserInfo>()
        userList.forEach {
            serversMap[it.nickName] = it
        }
        serversMap[newUser.nickName] = newUser
        return ArrayList(serversMap.values)
    }
}
