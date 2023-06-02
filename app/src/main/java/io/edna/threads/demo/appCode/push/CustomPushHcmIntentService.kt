package io.edna.threads.demo.appCode.push

import android.annotation.SuppressLint
import com.edna.android.push_lite.utils.CommonUtils
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import im.threads.business.serviceLocator.core.inject
import im.threads.ui.ChatCenterPushMessageHelper

class CustomPushHcmIntentService : HmsMessageService() {
    private val chatCenterPushMessageHelper: ChatCenterPushMessageHelper by inject()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        chatCenterPushMessageHelper.setHcmToken(token)
    }

    @SuppressLint("RestrictedApi")
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        chatCenterPushMessageHelper.process(
            this,
            CommonUtils.base64JsonStringToBundle(message.data)
        )
    }
}
