package io.edna.threads.demo.appCode.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import im.threads.business.serviceLocator.core.inject
import im.threads.ui.ChatCenterPushMessageHelper
import im.threads.ui.core.ThreadsLib
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CustomPushFcmIntentService : FirebaseMessagingService() {
    private val chatCenterPushMessageHelper: ChatCenterPushMessageHelper by inject()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            ThreadsLib.waitInitialization()
            chatCenterPushMessageHelper.setFcmToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        CoroutineScope(Dispatchers.IO).launch {
            ThreadsLib.waitInitialization()
            chatCenterPushMessageHelper.process(this@CustomPushFcmIntentService, message.data)
        }
    }
}
