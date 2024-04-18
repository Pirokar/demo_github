package edna.chatcenter.demo.appCode.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import edna.chatcenter.demo.appCode.EdnaChatCenterApplication

class CustomPushFcmIntentService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val application = applicationContext as EdnaChatCenterApplication
        application.chatCenterUI?.setFcmToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {}
}
