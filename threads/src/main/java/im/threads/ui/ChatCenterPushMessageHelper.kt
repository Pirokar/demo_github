package im.threads.ui

import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna
import im.threads.business.models.CAMPAIGN_DATE_FORMAT_PARSE
import im.threads.business.models.CampaignMessage
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.serviceLocator.core.inject
import im.threads.business.transport.CloudMessagingType
import im.threads.business.transport.MessageAttributes
import im.threads.business.transport.PushMessageAttributes
import im.threads.ui.workers.NotificationWorker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatCenterPushMessageHelper() {
    private val preferences: Preferences by inject()

    fun setFcmToken(fcmToken: String?) {
        val cloudMessagingType = preferences.get<String>(PreferencesCoreKeys.CLOUD_MESSAGING_TYPE)
        if (cloudMessagingType == null) {
            preferences.save(PreferencesCoreKeys.CLOUD_MESSAGING_TYPE, CloudMessagingType.FCM.toString())
        }
        if (fcmToken != preferences.get(PreferencesCoreKeys.FCM_TOKEN, "")) {
            preferences.save(PreferencesCoreKeys.FCM_TOKEN, fcmToken)
            BaseConfig.instance.transport.updatePushToken()
        }
    }

    fun setHcmToken(hcmToken: String?) {
        val cloudMessagingType = preferences.get<String>(PreferencesCoreKeys.CLOUD_MESSAGING_TYPE)
        if (cloudMessagingType == null) {
            preferences.save(PreferencesCoreKeys.CLOUD_MESSAGING_TYPE, CloudMessagingType.HCM.toString())
        }
        if (hcmToken != preferences.get(PreferencesCoreKeys.HCM_TOKEN, "")) {
            preferences.save(PreferencesCoreKeys.HCM_TOKEN, hcmToken)
            BaseConfig.instance.transport.updatePushToken()
        }
    }

    fun process(context: Context, data: Map<String, String>) {
        process(context, bundleOf(*data.toList().toTypedArray()))
    }

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
                    preferences.save(PreferencesCoreKeys.CAMPAIGN_MESSAGE, campaignMessage)
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
