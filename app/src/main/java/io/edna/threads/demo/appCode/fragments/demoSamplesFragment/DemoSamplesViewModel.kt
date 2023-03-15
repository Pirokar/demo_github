package io.edna.threads.demo.appCode.fragments.demoSamplesFragment

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import im.threads.ui.fragments.ChatFragment
import io.edna.threads.demo.appCode.business.SingleLiveEvent
import io.edna.threads.demo.appCode.business.mockJsonProvider.CurrentJsonProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class DemoSamplesViewModel(private val jsonProvider: CurrentJsonProvider) : ViewModel(), DefaultLifecycleObserver {
    private val chatFragmentMutableLiveData: SingleLiveEvent<ChatFragment> = SingleLiveEvent()
    val chatFragmentLiveData: LiveData<ChatFragment> get() = chatFragmentMutableLiveData

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val mockServer = MockWebServer()

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        coroutineScope.launch {
            enableMockServer()
            withContext(Dispatchers.Main) {
                if (isActive) prepareFragment()
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        mockServer.shutdown()
        coroutineScope.cancel()
    }

    private fun enableMockServer() {
        val json = jsonProvider.getCurrentJson()
        mockServer.enqueue(MockResponse().setBody(json))
        mockServer.start()
        mockServer.url("/history")
    }

    private fun prepareFragment() {
        chatFragmentMutableLiveData.value = ChatFragment.newInstance(1)
    }
}
