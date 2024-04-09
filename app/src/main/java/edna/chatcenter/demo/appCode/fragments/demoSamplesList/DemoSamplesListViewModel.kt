package edna.chatcenter.demo.appCode.fragments.demoSamplesList

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edna.chatcenter.demo.R
import edna.chatcenter.demo.appCode.business.StringsProvider
import edna.chatcenter.demo.appCode.business.VolatileLiveData
import edna.chatcenter.demo.appCode.business.mockJsonProvider.CurrentJsonProvider
import edna.chatcenter.demo.appCode.business.mockJsonProvider.SamplesJsonProvider
import edna.chatcenter.demo.appCode.models.DemoSamplesListItem
import edna.chatcenter.demo.appCode.models.DemoSamplesListItem.TEXT
import edna.chatcenter.ui.core.config.ChatUser
import edna.chatcenter.ui.visual.core.ChatCenterUI

class DemoSamplesListViewModel(
    private val stringsProvider: StringsProvider,
    private val samplesJsonProvider: SamplesJsonProvider,
    private val currentJsonProvider: CurrentJsonProvider
) : ViewModel(), DefaultLifecycleObserver {
    private val mutableDemoSamplesLiveData = MutableLiveData<List<DemoSamplesListItem>>()
    val demoSamplesLiveData: LiveData<List<DemoSamplesListItem>> = mutableDemoSamplesLiveData
    val navigationLiveData = VolatileLiveData<Int>()

    private var chatCenterUI: ChatCenterUI? = null

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        createData()
    }

    fun providerChatCenterUI(chatCenterUI: ChatCenterUI?) {
        this.chatCenterUI = chatCenterUI
    }

    fun onItemClick(item: DemoSamplesListItem) {
        if (item is TEXT) {
            currentJsonProvider.saveCurrentJson(item.json)
            chatCenterUI?.authorize(
                ChatUser("333"),
                null
            )
            navigationLiveData.setValue(R.id.action_DemoSamplesListFragment_to_DemoSamplesFragment)
        }
    }

    private fun createData() {
        mutableDemoSamplesLiveData.postValue(
            listOf(
                TEXT(stringsProvider.textMessages, samplesJsonProvider.getTextChatJson()),
                TEXT(stringsProvider.connectionErrors, samplesJsonProvider.getConnectionErrorJson()),
                TEXT(stringsProvider.voiceMessages, samplesJsonProvider.getVoicesChatJson()),
                TEXT(stringsProvider.images, samplesJsonProvider.getImagesChatJson()),
                TEXT(stringsProvider.files, samplesJsonProvider.getFilesChatJson()),
                TEXT(stringsProvider.systemMessages, samplesJsonProvider.getSystemChatJson()),
                TEXT(stringsProvider.chatWithBot, samplesJsonProvider.getChatBotJson()),
                TEXT(stringsProvider.chatWithEditAndDeletedMessages, samplesJsonProvider.getChatWithEditAndDeletedMessages())
            )
        )
    }
}
