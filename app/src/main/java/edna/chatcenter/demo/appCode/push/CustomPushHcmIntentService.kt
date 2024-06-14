package edna.chatcenter.demo.appCode.push

import android.annotation.SuppressLint
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import edna.chatcenter.demo.integrationCode.EdnaChatCenterApplication

class CustomPushHcmIntentService : HmsMessageService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val application = applicationContext as EdnaChatCenterApplication
        application.chatCenterUI?.setHcmToken(token)
    }

    @SuppressLint("RestrictedApi")
    override fun onMessageReceived(message: RemoteMessage) {}
}
