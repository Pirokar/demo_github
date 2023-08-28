package io.edna.threads.demo.appCode.push

import android.annotation.SuppressLint
import com.edna.android.push_lite.utils.CommonUtils
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import im.threads.business.serviceLocator.core.inject
import im.threads.ui.ChatCenterPushMessageHelper
import im.threads.ui.core.ThreadsLib
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CustomPushHcmIntentService : HmsMessageService() {
    private val chatCenterPushMessageHelper: ChatCenterPushMessageHelper by inject()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            ThreadsLib.waitInitialization()
            chatCenterPushMessageHelper.setHcmToken(token)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        CoroutineScope(Dispatchers.IO).launch {
            ThreadsLib.waitInitialization()
            chatCenterPushMessageHelper.process(
                this@CustomPushHcmIntentService,
                CommonUtils.base64JsonStringToBundle(message.data)
            )
        }
    }
}
