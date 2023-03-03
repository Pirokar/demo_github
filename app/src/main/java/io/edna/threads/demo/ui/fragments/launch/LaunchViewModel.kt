package io.edna.threads.demo.ui.fragments.launch

import androidx.lifecycle.ViewModel
import io.edna.threads.demo.utils.SingleLiveEvent

class LaunchViewModel : ViewModel() {
    val selectServerAction: SingleLiveEvent<String> = SingleLiveEvent()
    val selectUserAction: SingleLiveEvent<String> = SingleLiveEvent()
}
