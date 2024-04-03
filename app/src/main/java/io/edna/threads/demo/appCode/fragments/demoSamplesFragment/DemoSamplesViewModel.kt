package io.edna.threads.demo.appCode.fragments.demoSamplesFragment

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import edna.chatcenter.ui.core.annotation.OpenWay
import edna.chatcenter.ui.visual.fragments.ChatFragment
import io.edna.threads.demo.appCode.business.PreferencesProvider
import io.edna.threads.demo.appCode.business.SingleLiveEvent
import io.edna.threads.demo.appCode.business.mockJsonProvider.CurrentJsonProvider

class DemoSamplesViewModel(
    private val jsonProvider: CurrentJsonProvider,
    private val preferencesProvider: PreferencesProvider
) : ViewModel(), DefaultLifecycleObserver {
    private val chatFragmentMutableLiveData: SingleLiveEvent<ChatFragment> = SingleLiveEvent()
    val chatFragmentLiveData: LiveData<ChatFragment> get() = chatFragmentMutableLiveData

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        setJsonMock()
        prepareFragment()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        preferencesProvider.cleanJsonOnPreferences()
    }

    private fun setJsonMock() {
        val json = jsonProvider.getCurrentJson()
        preferencesProvider.putJsonToPreferences(json)
    }

    private fun prepareFragment() {
        chatFragmentMutableLiveData.value = ChatFragment.newInstance(OpenWay.FROM_PUSH)
    }
}
