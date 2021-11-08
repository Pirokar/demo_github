package im.threads.internal.transport.mfms_push

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import im.threads.internal.chat_updates.ChatUpdateProcessor
import im.threads.internal.database.DatabaseHolder
import im.threads.internal.formatters.ChatItemType
import im.threads.internal.model.CAMPAIGN_DATE_FORMAT_PARSE
import im.threads.internal.model.CampaignMessage
import im.threads.internal.model.SearchingConsult
import im.threads.internal.model.UserPhrase
import im.threads.internal.services.NotificationService
import im.threads.internal.utils.PrefUtils
import im.threads.internal.utils.ThreadsLogger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShortPushMessageProcessingDelegate {

    fun process(context: Context, bundle: Bundle, alert: String?) {
        val sdf = SimpleDateFormat(CAMPAIGN_DATE_FORMAT_PARSE, Locale.getDefault())
        if (MFMSPushMessageParser.isThreadsOriginPush(bundle)) {
            when (getKnownType(bundle)) {
                ChatItemType.TYPING -> {
                    val clientId: String? = bundle.getString(PushMessageAttributes.CLIENT_ID)
                    if (clientId != null) {
                        ChatUpdateProcessor.getInstance().postTyping(clientId)
                    }
                }
                ChatItemType.MESSAGES_READ -> {
                    val readMessagesIds = MFMSPushMessageParser.getReadIds(bundle)
                    ThreadsLogger.i(
                        TAG,
                        "onSystemMessageFromServer: read messages $readMessagesIds"
                    )
                    for (readId in readMessagesIds) {
                        val userPhrase =
                            DatabaseHolder.getInstance().getChatItem(readId) as UserPhrase?
                        userPhrase?.providerId?.let {
                            ChatUpdateProcessor.getInstance()
                                .postOutgoingMessageWasRead(it)
                        }
                    }
                }
                ChatItemType.OPERATOR_LOOKUP_STARTED -> ChatUpdateProcessor.getInstance()
                    .postNewMessage(SearchingConsult())
                ChatItemType.UNREAD_MESSAGE_NOTIFICATION -> {
                    val operatorUrl: String? = bundle.getString(PushMessageAttributes.OPERATOR_URL)
                    val appMarker: String? = bundle.getString(PushMessageAttributes.APP_MARKER_KEY)
                    NotificationService.addUnreadMessage(context, Date().hashCode(), alert, operatorUrl, appMarker)
                }
                else -> {
                    if (bundle.containsKey(PushMessageAttributes.GATE_MESSAGE_ID)) {
                        val alertStr = alert ?: ""
                        val campaign = bundle.getString(PushMessageAttributes.CAMPAIGN) ?: ""
                        val senderName = bundle.getString(PushMessageAttributes.SENDER_NAME) ?: ""
                        PrefUtils.setCampaignMessage(
                            CampaignMessage(
                                alertStr,
                                senderName,
                                Date(),
                                bundle.getString(PushMessageAttributes.GATE_MESSAGE_ID)?.toLong()
                                    ?: 0,
                                bundle.getString(PushMessageAttributes.EXPIRED_AT)
                                    ?.let { sdf.parse(it) }
                                    ?: Date(),
                                bundle.getString(PushMessageAttributes.SKILL_ID)?.toInt() ?: 0,
                                campaign,
                                bundle.getString(PushMessageAttributes.PRIORITY)?.toInt() ?: 0,
                            )
                        )
                        NotificationService.addCampaignMessage(context, alert)
                    }
                    ThreadsLogger.i(
                        TAG,
                        "Unknown notification type"
                    )
                }
            }
        }
    }

    private fun getKnownType(bundle: Bundle?): ChatItemType {
        if (bundle == null) {
            return ChatItemType.UNKNOWN
        }
        if (bundle.containsKey(PushMessageAttributes.READ_PROVIDER_IDS)) {
            return ChatItemType.MESSAGES_READ
        }
        val pushType = bundle.getString(PushMessageAttributes.TYPE)
        if (!TextUtils.isEmpty(pushType)) {
            val chatItemType = ChatItemType.fromString(pushType)
            if (chatItemType == ChatItemType.OPERATOR_JOINED || chatItemType == ChatItemType.OPERATOR_LEFT || chatItemType == ChatItemType.TYPING || chatItemType == ChatItemType.SCHEDULE || chatItemType == ChatItemType.SURVEY || chatItemType == ChatItemType.REQUEST_CLOSE_THREAD || chatItemType == ChatItemType.REMOVE_PUSHES || chatItemType == ChatItemType.UNREAD_MESSAGE_NOTIFICATION || chatItemType == ChatItemType.OPERATOR_LOOKUP_STARTED) {
                return chatItemType
            }
        }
        return ChatItemType.CHAT_PUSH
    }

    private const val TAG = "ShortPushMessageProcessingDelegate"
}