package io.edna.threads.demo.appCode.fragments.demoSamplesList

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.StringsProvider
import io.edna.threads.demo.appCode.business.mockJsonProvider.CurrentJsonProvider
import io.edna.threads.demo.appCode.business.mockJsonProvider.SamplesJsonProvider
import io.edna.threads.demo.appCode.models.DemoSamplesListItem
import io.edna.threads.demo.appCode.models.DemoSamplesListItem.TEXT

class DemoSamplesListViewModel(
    private val stringsProvider: StringsProvider,
    private val samplesJsonProvider: SamplesJsonProvider,
    private val currentJsonProvider: CurrentJsonProvider
) : ViewModel(), DefaultLifecycleObserver {
    private val mutableDemoSamplesLiveData = MutableLiveData<List<DemoSamplesListItem>>()
    val demoSamplesLiveData: LiveData<List<DemoSamplesListItem>> = mutableDemoSamplesLiveData

    private val mutableNavigationLiveData = MutableLiveData<Int>()
    val navigationLiveData = mutableNavigationLiveData

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        createData()
    }

    fun onItemClick(item: DemoSamplesListItem) {
        if (item is TEXT) {
            currentJsonProvider.saveCurrentJson(item.json)
            mutableNavigationLiveData.value = R.id.action_DemoSamplesListFragment_to_DemoSamplesFragment
        }
    }

    private fun createData() {
        mutableDemoSamplesLiveData.postValue(
            listOf(
                TEXT(stringsProvider.textMessages, samplesJsonProvider.getTextChatJson()),
                TEXT(stringsProvider.connectionErrors, samplesJsonProvider.getConnectionErrorJson()),
                TEXT(stringsProvider.voiceMessages, samplesJsonProvider.getVoicesChatJson()),
                TEXT(stringsProvider.images, samplesJsonProvider.getImagesChatJson()),
                TEXT(stringsProvider.systemMessages, samplesJsonProvider.getSystemChatJson()),
                TEXT(stringsProvider.chatWithBot, samplesJsonProvider.getChatBotJson())
            )
        )
    }
}
