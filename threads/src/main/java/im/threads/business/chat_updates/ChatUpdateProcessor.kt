package im.threads.business.chat_updates

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
import im.threads.ui.controllers.ChatController
import im.threads.ui.utils.FileHelper.subscribeToAttachments
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor

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

    /**
     * Передаёт ответы от коллбэков сокетного соединения.
     * Предоставляется с целью обработки ошибок разрыва соединений.
     */
    val socketResponseMapProcessor: FlowableProcessor<Map<String, Any>> = PublishProcessor.create()
    fun postTyping(clientId: String) {
        typingProcessor.onNext(clientId)
    }

    fun postAttachmentSettings(attachmentSettings: AttachmentSettings) {
        subscribeToAttachments()
        checkSubscribers()
        attachmentSettingsProcessor.onNext(attachmentSettings)
    }

    fun postOutgoingMessageStatusChanged(statuses: List<Status>) {
        checkSubscribers()
        outgoingMessageStatusChangedProcessor.onNext(statuses)
    }

    fun postIncomingMessageWasRead(messageId: String) {
        checkSubscribers()
        incomingMessageReadProcessor.onNext(messageId)
    }

    fun postSpeechMessageUpdate(speechMessageUpdate: SpeechMessageUpdate) {
        checkSubscribers()
        speechMessageUpdateProcessor.onNext(speechMessageUpdate)
    }

    fun postNewMessage(chatItem: ChatItem) {
        checkSubscribers()
        newMessageProcessor.onNext(chatItem)
    }

    fun updateAttachments(attachments: List<Attachment>) {
        checkSubscribers()
        updateAttachmentsProcessor.onNext(attachments)
    }

    fun postChatItemSendSuccess(chatItemProviderData: ChatItemProviderData) {
        checkSubscribers()
        messageSendSuccessProcessor.onNext(chatItemProviderData)
    }

    fun postChatItemSendError(sendErrorModel: ChatItemSendErrorModel) {
        checkSubscribers()
        messageSendErrorProcessor.onNext(sendErrorModel)
    }

    fun postRemoveChatItem(chatItemType: ChatItemType) {
        checkSubscribers()
        removeChatItemProcessor.onNext(chatItemType)
    }

    fun postSurveySendSuccess(survey: Survey) {
        checkSubscribers()
        surveySendSuccessProcessor.onNext(survey)
    }

    fun postCampaignMessageReplySuccess(campaignMessage: CampaignMessage) {
        checkSubscribers()
        campaignMessageReplySuccessProcessor.onNext(campaignMessage)
    }

    fun postDeviceAddressChanged(deviceAddress: String) {
        checkSubscribers()
        deviceAddressChangedProcessor.onNext(deviceAddress)
    }

    fun postUserInputEnableChanged(enable: InputFieldEnableModel) {
        checkSubscribers()
        userInputEnableProcessor.onNext(enable)
    }

    fun postQuickRepliesChanged(quickReplies: QuickReplyItem) {
        checkSubscribers()
        quickRepliesProcessor.onNext(quickReplies)
    }

    fun postClientNotificationDisplayType(type: ClientNotificationDisplayType) {
        checkSubscribers()
        clientNotificationDisplayTypeProcessor.onNext(type)
    }

    fun postAttachAudioFile(attached: Boolean) {
        checkSubscribers()
        attachAudioFilesProcessor.onNext(attached)
    }

    fun postError(error: TransportException) {
        checkSubscribers()
        errorProcessor.onNext(error)
    }

    fun postSocketResponseMap(socketResponseMap: Map<String, Any>) {
        checkSubscribers()
        socketResponseMapProcessor.onNext(socketResponseMap)
    }

    fun postUploadResult(uploadResult: FileDescription?) {
        checkSubscribers()
        uploadResultProcessor.onNext(uploadResult)
    }

    private fun checkSubscribers() {
        ChatController.getInstance().checkSubscribing()
    }
}
