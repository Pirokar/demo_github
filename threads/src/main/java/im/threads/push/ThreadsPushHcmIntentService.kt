package im.threads.push

import com.edna.android.push_lite.huawei.HcmPushService
import com.edna.android.push_lite.utils.CommonUtils
import com.huawei.hms.push.RemoteMessage
import im.threads.ConfigBuilder
import im.threads.internal.Config
import im.threads.internal.services.NotificationService
import im.threads.internal.transport.MessageAttributes
import im.threads.internal.transport.mfms_push.ShortPushMessageProcessingDelegate.process
import im.threads.internal.utils.PrefUtils
import java.util.Date

class ThreadsPushHcmIntentService : HcmPushService() {

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        PrefUtils.setHcmToken(newToken)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = CommonUtils.base64JsonStringToBundle(message.data)
        if (Config.instance.transport.type == ConfigBuilder.TransportType.MFMS_PUSH) {
            if (data.containsKey(CHL_SENT_AT) && (!data.containsKey(
                    MESSAGE_ID
                ) || !data.containsKey(SERVER_MESSAGE_ID))
            ) {
                process(this, data, null)
            }
        } else if (Config.instance.transport.type == ConfigBuilder.TransportType.THREADS_GATE) {
            if (MessageAttributes.THREADS == data[MessageAttributes.ORIGIN]) {
                val operatorUrl = data[MessageAttributes.OPERATOR_URL] as String?
                val appMarker = data[MessageAttributes.APP_MARKER_KEY] as String?
                val text = (data[MessageAttributes.MESSAGE] ?: data[MessageAttributes.ALERT]) as String?
                NotificationService.addUnreadMessage(
                    this,
                    Date().hashCode(),
                    text,
                    operatorUrl,
                    appMarker
                )
            }
        }
    }

    companion object {
        private const val MESSAGE_ID = "messageId"
        private const val SERVER_MESSAGE_ID = "serverMessageId"
        private const val CHL_SENT_AT = "chlSentAt"
    }
}
