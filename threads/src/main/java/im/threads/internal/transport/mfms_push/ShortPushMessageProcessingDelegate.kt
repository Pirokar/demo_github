package im.threads.internal.transport.mfms_push

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import im.threads.internal.chat_updates.ChatUpdateProcessor
import im.threads.internal.database.DatabaseHolder
import im.threads.internal.formatters.ChatItemType
import im.threads.internal.model.SearchingConsult
import im.threads.internal.model.UserPhrase
import im.threads.internal.services.NotificationService
import im.threads.internal.utils.ThreadsLogger

object ShortPushMessageProcessingDelegate {
    fun process(context: Context, bundle: Bundle, alert: String?) {
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
                        if (userPhrase != null) {
                            ChatUpdateProcessor.getInstance()
                                .postOutgoingMessageWasRead(userPhrase.providerId)
                        }
                    }
                }
                ChatItemType.REMOVE_PUSHES -> NotificationService.removeNotification(context)
                ChatItemType.OPERATOR_LOOKUP_STARTED -> ChatUpdateProcessor.getInstance()
                    .postNewMessage(SearchingConsult())
                ChatItemType.UNREAD_MESSAGE_NOTIFICATION -> {
                    val operatorUrl: String? = bundle.getString(PushMessageAttributes.OPERATOR_URL)
                    val appMarker: String? = bundle.getString(PushMessageAttributes.APP_MARKER_KEY)
                    NotificationService.addUnreadMessage(context, alert, operatorUrl, appMarker)
                }
                else -> {
                    ThreadsLogger.i(
                        TAG,
                        "Unl"
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
        // old push format
        if (pushType == null && bundle.getString("alert") != null && bundle.getString("advisa") == null && bundle.getString(
                "GEO_FENCING"
            ) == null
        ) {
            return ChatItemType.MESSAGE
        }
        return if (MFMSPushMessageParser.isThreadsOriginPush(bundle)) {
            ChatItemType.CHAT_PUSH
        } else ChatItemType.UNKNOWN
    }

    private const val TAG = "ShortPushMessageProcessingDelegate"
}