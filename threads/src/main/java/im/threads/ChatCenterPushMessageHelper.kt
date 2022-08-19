package im.threads

import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import im.threads.internal.domain.logger.LoggerEdna
import im.threads.internal.model.CAMPAIGN_DATE_FORMAT_PARSE
import im.threads.internal.model.CampaignMessage
import im.threads.internal.transport.MessageAttributes
import im.threads.internal.transport.PushMessageAttributes
import im.threads.internal.utils.PrefUtils
import im.threads.internal.workers.NotificationWorker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ChatCenterPushMessageHelper {

    @JvmStatic
    fun setFcmToken(fcmToken: String?) {
        PrefUtils.fcmToken = fcmToken
    }

    @JvmStatic
    fun setHcmToken(hcmToken: String?) {
        PrefUtils.hcmToken = hcmToken
    }

    @JvmStatic
    fun process(context: Context, data: Map<String, String>) {
        process(context, bundleOf(*data.toList().toTypedArray()))
    }

    @JvmStatic
    fun process(context: Context, bundle: Bundle) {
        val sdf = SimpleDateFormat(CAMPAIGN_DATE_FORMAT_PARSE, Locale.getDefault())
        if (PushMessageAttributes.THREADS.equals(
                bundle.getString(
                        PushMessageAttributes.ORIGIN
                    ),
                ignoreCase = true
            )
        ) {
            when {
                bundle.containsKey(PushMessageAttributes.GATE_MESSAGE_ID) -> {
                    val alertStr = bundle.getString(PushMessageAttributes.ALERT) ?: ""
                    val campaign = bundle.getString(PushMessageAttributes.CAMPAIGN) ?: ""
                    val senderName = bundle.getString(PushMessageAttributes.SENDER_NAME) ?: ""
                    val campaignMessage = CampaignMessage(
                        alertStr,
                        senderName,
                        Date(),
                        bundle.getString(PushMessageAttributes.CHAT_MESSAGE_ID)
                            ?: "",
                        bundle.getString(PushMessageAttributes.GATE_MESSAGE_ID)?.toLong()
                            ?: 0,
                        bundle.getString(PushMessageAttributes.EXPIRED_AT)
                            ?.let { sdf.parse(it) }
                            ?: Date(),
                        bundle.getString(MessageAttributes.SKILL_ID)?.toInt() ?: 0,
                        campaign,
                        bundle.getString(MessageAttributes.PRIORITY)?.toInt() ?: 0
                    )
                    PrefUtils.campaignMessage = campaignMessage
                    NotificationWorker.addCampaignMessage(context, alertStr)
                    LoggerEdna.info("campaign message handled: $campaignMessage")
                }
                bundle.containsKey(PushMessageAttributes.MESSAGE) || bundle.containsKey(PushMessageAttributes.ALERT) -> {
                    val operatorUrl = bundle[PushMessageAttributes.OPERATOR_URL] as String?
                    val appMarker = bundle[PushMessageAttributes.APP_MARKER_KEY] as String?
                    val text = bundle[PushMessageAttributes.MESSAGE] as String? ?: bundle[PushMessageAttributes.ALERT] as String?
                    NotificationWorker.addUnreadMessage(
                        context,
                        Date().hashCode(),
                        text,
                        operatorUrl,
                        appMarker
                    )
                    LoggerEdna.info("text message handled: $text")
                }
                else -> {
                    LoggerEdna.info("unparsed message with origin=threads ")
                }
            }
        } else {
            LoggerEdna.info("origin=threads not found in bundle")
        }
    }
}
