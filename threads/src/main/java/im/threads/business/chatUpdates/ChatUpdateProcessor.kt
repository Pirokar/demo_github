package im.threads.business.chatUpdates

import im.threads.business.formatters.ChatItemType
import im.threads.business.models.CampaignMessage
import im.threads.business.models.ChatItem
import im.threads.business.models.ChatItemSendErrorModel
import im.threads.business.models.ClientNotificationDisplayType
import im.threads.business.models.FileDescription
import im.threads.business.models.InputFieldEnableModel
import im.threads.business.models.QuickReplyItem
import im.threads.business.models.SpeechMessageUpdate
import im.threads.business.models.Survey
import im.threads.business.transport.ChatItemProviderData
import im.threads.business.transport.TransportException
import im.threads.business.transport.models.Attachment
import im.threads.business.transport.models.AttachmentSettings
import im.threads.business.transport.threadsGate.responses.Status
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class ChatUpdateProcessor {

    val typingProcessor: FlowableProcessor<String> = PublishProcessor.create()
    val attachmentSettingsProcessor: FlowableProcessor<AttachmentSettings> = PublishProcessor.create()
    val outgoingMessageStatusChangedProcessor: FlowableProcessor<List<Status>> = PublishProcessor.create()
    val incomingMessageReadProcessor: FlowableProcessor<String> = PublishProcessor.create()
    val newMessageProcessor: FlowableProcessor<ChatItem> = PublishProcessor.create()
    val updateAttachmentsProcessor: FlowableProcessor<List<Attachment>> = PublishProcessor.create()
    val messageSendSuccessProcessor: FlowableProcessor<ChatItemProviderData> = PublishProcessor.create()
    val campaignMessageReplySuccessProcessor: FlowableProcessor<CampaignMessage> = PublishProcessor.create()
    val messageSendErrorProcessor: FlowableProcessor<ChatItemSendErrorModel> = PublishProcessor.create()
    val removeChatItemProcessor: FlowableProcessor<ChatItemType> = PublishProcessor.create()
    val surveySendSuccessProcessor: FlowableProcessor<Survey> = PublishProcessor.create()
    val deviceAddressChangedProcessor: FlowableProcessor<String> = PublishProcessor.create()
    val userInputEnableProcessor: FlowableProcessor<InputFieldEnableModel> = PublishProcessor.create()
    val quickRepliesProcessor: FlowableProcessor<QuickReplyItem> = PublishProcessor.create()
    val clientNotificationDisplayTypeProcessor: FlowableProcessor<ClientNotificationDisplayType> = PublishProcessor.create()
    val speechMessageUpdateProcessor: FlowableProcessor<SpeechMessageUpdate> = PublishProcessor.create()
    val attachAudioFilesProcessor: FlowableProcessor<Boolean> = PublishProcessor.create()
    val errorProcessor: FlowableProcessor<TransportException> = PublishProcessor.create()
    val uploadResultProcessor: FlowableProcessor<FileDescription> = PublishProcessor.create()
    val grantedPermissionsFlow: MutableSharedFlow<Int> = MutableSharedFlow()

    /**
     * Передаёт ответы от коллбэков сокетного соединения.
     * Предоставляется с целью обработки ошибок разрыва соединений.
     */
    val socketResponseMapProcessor: FlowableProcessor<Map<String, Any>> = PublishProcessor.create()
    fun postTyping() {
        typingProcessor.onNext("")
    }

    fun postAttachmentSettings(attachmentSettings: AttachmentSettings) {
        attachmentSettingsProcessor.onNext(attachmentSettings)
    }

    fun postOutgoingMessageStatusChanged(statuses: List<Status>) {
        outgoingMessageStatusChangedProcessor.onNext(statuses)
    }

    fun postIncomingMessageWasRead(messageId: String) {
        incomingMessageReadProcessor.onNext(messageId)
    }

    fun postSpeechMessageUpdate(speechMessageUpdate: SpeechMessageUpdate) {
        speechMessageUpdateProcessor.onNext(speechMessageUpdate)
    }

    fun postNewMessage(chatItem: ChatItem) {
        newMessageProcessor.onNext(chatItem)
    }

    fun updateAttachments(attachments: List<Attachment>) {
        updateAttachmentsProcessor.onNext(attachments)
    }

    fun postChatItemSendSuccess(chatItemProviderData: ChatItemProviderData) {
        messageSendSuccessProcessor.onNext(chatItemProviderData)
    }

    fun postChatItemSendError(sendErrorModel: ChatItemSendErrorModel) {
        messageSendErrorProcessor.onNext(sendErrorModel)
    }

    fun postRemoveChatItem(chatItemType: ChatItemType) {
        removeChatItemProcessor.onNext(chatItemType)
    }

    fun postSurveySendSuccess(survey: Survey) {
        surveySendSuccessProcessor.onNext(survey)
    }

    fun postCampaignMessageReplySuccess(campaignMessage: CampaignMessage) {
        campaignMessageReplySuccessProcessor.onNext(campaignMessage)
    }

    fun postDeviceAddressChanged(deviceAddress: String) {
        deviceAddressChangedProcessor.onNext(deviceAddress)
    }

    fun postUserInputEnableChanged(enable: InputFieldEnableModel) {
        userInputEnableProcessor.onNext(enable)
    }

    fun postQuickRepliesChanged(quickReplies: QuickReplyItem) {
        quickRepliesProcessor.onNext(quickReplies)
    }

    fun postClientNotificationDisplayType(type: ClientNotificationDisplayType) {
        clientNotificationDisplayTypeProcessor.onNext(type)
    }

    fun postAttachAudioFile(attached: Boolean) {
        attachAudioFilesProcessor.onNext(attached)
    }

    fun postError(error: TransportException) {
        errorProcessor.onNext(error)
    }

    fun postSocketResponseMap(socketResponseMap: Map<String, Any>) {
        socketResponseMapProcessor.onNext(socketResponseMap)
    }

    fun postUploadResult(uploadResult: FileDescription?) {
        uploadResultProcessor.onNext(uploadResult)
    }

    fun postGrantedPermissions(permissionsRequestCode: Int) {
        CoroutineScope(Dispatchers.Unconfined).launch {
            grantedPermissionsFlow.emit(permissionsRequestCode)
        }
    }
}
