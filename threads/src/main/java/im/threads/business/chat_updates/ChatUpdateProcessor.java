package im.threads.business.chat_updates;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

import im.threads.business.models.CampaignMessage;
import im.threads.business.models.ChatItem;
import im.threads.business.models.SpeechMessageUpdate;
import im.threads.business.models.Survey;
import im.threads.business.transport.ChatItemProviderData;
import im.threads.business.transport.TransportException;
import im.threads.business.transport.models.Attachment;
import im.threads.business.transport.models.AttachmentSettings;
import im.threads.business.formatters.ChatItemType;
import im.threads.business.models.ChatItemSendErrorModel;
import im.threads.business.models.ClientNotificationDisplayType;
import im.threads.business.models.InputFieldEnableModel;
import im.threads.business.models.QuickReplyItem;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public class ChatUpdateProcessor {

    private static ChatUpdateProcessor instance;

    private final FlowableProcessor<String> typingProcessor = PublishProcessor.create();
    private final FlowableProcessor<AttachmentSettings> attachmentSettingsProcessor = PublishProcessor.create();
    private final FlowableProcessor<String> outgoingMessageReadProcessor = PublishProcessor.create();
    private final FlowableProcessor<String> incomingMessageReadProcessor = PublishProcessor.create();
    private final FlowableProcessor<ChatItem> newMessageProcessor = PublishProcessor.create();
    private final FlowableProcessor<List<Attachment>> updateAttachmentsProcessor = PublishProcessor.create();
    private final FlowableProcessor<ChatItemProviderData> messageSendSuccessProcessor = PublishProcessor.create();
    private final FlowableProcessor<CampaignMessage> campaignMessageReplySuccessProcessor = PublishProcessor.create();
    private final FlowableProcessor<ChatItemSendErrorModel> messageSendErrorProcessor = PublishProcessor.create();
    private final FlowableProcessor<ChatItemType> removeChatItemProcessor = PublishProcessor.create();
    private final FlowableProcessor<Survey> surveySendSuccessProcessor = PublishProcessor.create();
    private final FlowableProcessor<String> deviceAddressChangedProcessor = PublishProcessor.create();
    private final FlowableProcessor<InputFieldEnableModel> userInputEnableProcessor = PublishProcessor.create();
    private final FlowableProcessor<QuickReplyItem> quickRepliesProcessor = PublishProcessor.create();
    private final FlowableProcessor<ClientNotificationDisplayType> clientNotificationDisplayTypeProcessor = PublishProcessor.create();
    private final FlowableProcessor<SpeechMessageUpdate> speechMessageUpdateProcessor = PublishProcessor.create();
    private final FlowableProcessor<Boolean> attachAudioFilesProcessor = PublishProcessor.create();
    private final FlowableProcessor<TransportException> errorProcessor = PublishProcessor.create();

    /**
     * Передаёт ответы от коллбэков сокетного соединения.
     * Предоставляется с целью обработки ошибок разрыва соединений.
     */
    private final FlowableProcessor<Map<String, Object>> socketResponseMapProcessor =
            PublishProcessor.create();

    public void postTyping(@NonNull String clientId) {
        typingProcessor.onNext(clientId);
    }

    public void postAttachmentSettings(@NonNull AttachmentSettings attachmentSettings) {
        attachmentSettingsProcessor.onNext(attachmentSettings);
    }

    public void postOutgoingMessageWasRead(@NonNull String messageId) {
        outgoingMessageReadProcessor.onNext(messageId);
    }

    public void postIncomingMessageWasRead(@NonNull String messageId) {
        incomingMessageReadProcessor.onNext(messageId);
    }

    public void postSpeechMessageUpdate(@NonNull SpeechMessageUpdate speechMessageUpdate) {
        speechMessageUpdateProcessor.onNext(speechMessageUpdate);
    }

    public void postNewMessage(@NonNull ChatItem chatItem) {
        newMessageProcessor.onNext(chatItem);
    }

    public void updateAttachments(@NonNull List<Attachment> attachments) {
        updateAttachmentsProcessor.onNext(attachments);
    }

    public void postChatItemSendSuccess(@NonNull ChatItemProviderData chatItemProviderData) {
        messageSendSuccessProcessor.onNext(chatItemProviderData);
    }

    public void postChatItemSendError(@NonNull ChatItemSendErrorModel sendErrorModel) {
        messageSendErrorProcessor.onNext(sendErrorModel);
    }

    public void postRemoveChatItem(@NonNull ChatItemType chatItemType) {
        removeChatItemProcessor.onNext(chatItemType);
    }

    public void postSurveySendSuccess(Survey survey) {
        surveySendSuccessProcessor.onNext(survey);
    }

    public void postCampaignMessageReplySuccess(CampaignMessage campaignMessage) {
        campaignMessageReplySuccessProcessor.onNext(campaignMessage);
    }

    public void postDeviceAddressChanged(String deviceAddress) {
        deviceAddressChangedProcessor.onNext(deviceAddress);
    }

    public void postUserInputEnableChanged(InputFieldEnableModel enable) {
        userInputEnableProcessor.onNext(enable);
    }

    public void postQuickRepliesChanged(QuickReplyItem quickReplies) {
        quickRepliesProcessor.onNext(quickReplies);
    }

    public void postClientNotificationDisplayType(ClientNotificationDisplayType type) {
        clientNotificationDisplayTypeProcessor.onNext(type);
    }

    public void postAttachAudioFile(Boolean attached) {
        attachAudioFilesProcessor.onNext(attached);
    }

    public void postError(@NonNull TransportException error) {
        errorProcessor.onNext(error);
    }

    public void postSocketResponseMap(@NonNull Map<String, Object> socketResponseMap) {
        socketResponseMapProcessor.onNext(socketResponseMap);
    }

    public FlowableProcessor<String> getTypingProcessor() {
        return typingProcessor;
    }

    public FlowableProcessor<AttachmentSettings> getAttachmentSettingsProcessor() {
        return attachmentSettingsProcessor;
    }

    public FlowableProcessor<String> getOutgoingMessageReadProcessor() {
        return outgoingMessageReadProcessor;
    }

    public FlowableProcessor<String> getIncomingMessageReadProcessor() {
        return incomingMessageReadProcessor;
    }

    public FlowableProcessor<SpeechMessageUpdate> getSpeechMessageUpdateProcessor() {
        return speechMessageUpdateProcessor;
    }

    public FlowableProcessor<ChatItem> getNewMessageProcessor() {
        return newMessageProcessor;
    }

    public FlowableProcessor<List<Attachment>> getUpdateAttachmentsProcessor() {
        return updateAttachmentsProcessor;
    }

    public FlowableProcessor<ChatItemProviderData> getMessageSendSuccessProcessor() {
        return messageSendSuccessProcessor;
    }

    public FlowableProcessor<ChatItemSendErrorModel> getMessageSendErrorProcessor() {
        return messageSendErrorProcessor;
    }

    public FlowableProcessor<ChatItemType> getRemoveChatItemProcessor() {
        return removeChatItemProcessor;
    }

    public FlowableProcessor<Survey> getSurveySendSuccessProcessor() {
        return surveySendSuccessProcessor;
    }

    public FlowableProcessor<CampaignMessage> getCampaignMessageReplySuccessProcessor() {
        return campaignMessageReplySuccessProcessor;
    }

    public FlowableProcessor<String> getDeviceAddressChangedProcessor() {
        return deviceAddressChangedProcessor;
    }

    public FlowableProcessor<InputFieldEnableModel> getUserInputEnableProcessor() {
        return userInputEnableProcessor;
    }

    public FlowableProcessor<QuickReplyItem> getQuickRepliesProcessor() {
        return quickRepliesProcessor;
    }

    public FlowableProcessor<ClientNotificationDisplayType> getClientNotificationDisplayTypeProcessor() {
        return clientNotificationDisplayTypeProcessor;
    }

    public FlowableProcessor<Boolean> getAttachAudioFilesProcessor() {
        return attachAudioFilesProcessor;
    }

    public FlowableProcessor<TransportException> getErrorProcessor() {
        return errorProcessor;
    }

    public FlowableProcessor<Map<String, Object>> getSocketResponseMapProcessor() {
        return socketResponseMapProcessor;
    }
}
