package im.threads.business.chat_updates;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

import im.threads.business.formatters.ChatItemType;
import im.threads.business.models.CampaignMessage;
import im.threads.business.models.ChatItem;
import im.threads.business.models.ChatItemSendErrorModel;
import im.threads.business.models.ClientNotificationDisplayType;
import im.threads.business.models.FileDescription;
import im.threads.business.models.InputFieldEnableModel;
import im.threads.business.models.QuickReplyItem;
import im.threads.business.models.SpeechMessageUpdate;
import im.threads.business.models.Survey;
import im.threads.business.transport.ChatItemProviderData;
import im.threads.business.transport.TransportException;
import im.threads.business.transport.models.Attachment;
import im.threads.business.transport.models.AttachmentSettings;
import im.threads.business.transport.threadsGate.responses.Status;
import im.threads.ui.controllers.ChatController;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public class ChatUpdateProcessor {

    private static ChatUpdateProcessor instance;

    private final FlowableProcessor<String> typingProcessor = PublishProcessor.create();
    private final FlowableProcessor<AttachmentSettings> attachmentSettingsProcessor = PublishProcessor.create();
    private final FlowableProcessor<List<Status>> outgoingMessageStatusChangedProcessor = PublishProcessor.create();
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
    private final FlowableProcessor<FileDescription> uploadResultProcessor = PublishProcessor.create();

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
        checkSubscribers();
        attachmentSettingsProcessor.onNext(attachmentSettings);
    }

    public void postOutgoingMessageStatusChanged(@NonNull List<Status> statuses) {
        checkSubscribers();
        outgoingMessageStatusChangedProcessor.onNext(statuses);
    }

    public void postIncomingMessageWasRead(@NonNull String messageId) {
        checkSubscribers();
        incomingMessageReadProcessor.onNext(messageId);
    }

    public void postSpeechMessageUpdate(@NonNull SpeechMessageUpdate speechMessageUpdate) {
        checkSubscribers();
        speechMessageUpdateProcessor.onNext(speechMessageUpdate);
    }

    public void postNewMessage(@NonNull ChatItem chatItem) {
        checkSubscribers();
        newMessageProcessor.onNext(chatItem);
    }

    public void updateAttachments(@NonNull List<Attachment> attachments) {
        checkSubscribers();
        updateAttachmentsProcessor.onNext(attachments);
    }

    public void postChatItemSendSuccess(@NonNull ChatItemProviderData chatItemProviderData) {
        checkSubscribers();
        messageSendSuccessProcessor.onNext(chatItemProviderData);
    }

    public void postChatItemSendError(@NonNull ChatItemSendErrorModel sendErrorModel) {
        checkSubscribers();
        messageSendErrorProcessor.onNext(sendErrorModel);
    }

    public void postRemoveChatItem(@NonNull ChatItemType chatItemType) {
        checkSubscribers();
        removeChatItemProcessor.onNext(chatItemType);
    }

    public void postSurveySendSuccess(Survey survey) {
        checkSubscribers();
        surveySendSuccessProcessor.onNext(survey);
    }

    public void postCampaignMessageReplySuccess(CampaignMessage campaignMessage) {
        checkSubscribers();
        campaignMessageReplySuccessProcessor.onNext(campaignMessage);
    }

    public void postDeviceAddressChanged(String deviceAddress) {
        checkSubscribers();
        deviceAddressChangedProcessor.onNext(deviceAddress);
    }

    public void postUserInputEnableChanged(InputFieldEnableModel enable) {
        checkSubscribers();
        userInputEnableProcessor.onNext(enable);
    }

    public void postQuickRepliesChanged(QuickReplyItem quickReplies) {
        checkSubscribers();
        quickRepliesProcessor.onNext(quickReplies);
    }

    public void postClientNotificationDisplayType(ClientNotificationDisplayType type) {
        checkSubscribers();
        clientNotificationDisplayTypeProcessor.onNext(type);
    }

    public void postAttachAudioFile(Boolean attached) {
        checkSubscribers();
        attachAudioFilesProcessor.onNext(attached);
    }

    public void postError(@NonNull TransportException error) {
        checkSubscribers();
        errorProcessor.onNext(error);
    }

    public void postSocketResponseMap(@NonNull Map<String, Object> socketResponseMap) {
        checkSubscribers();
        socketResponseMapProcessor.onNext(socketResponseMap);
    }

    public void postUploadResult(@NonNull FileDescription uploadResult) {
        checkSubscribers();
        uploadResultProcessor.onNext(uploadResult);
    }

    public FlowableProcessor<FileDescription> getUploadResultProcessor() {
        return uploadResultProcessor;
    }

    public FlowableProcessor<String> getTypingProcessor() {
        return typingProcessor;
    }

    public FlowableProcessor<AttachmentSettings> getAttachmentSettingsProcessor() {
        return attachmentSettingsProcessor;
    }

    public FlowableProcessor<List<Status>> getOutgoingMessageStatusChangedProcessor() {
        return outgoingMessageStatusChangedProcessor;
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

    private void checkSubscribers() {
        ChatController.getInstance().checkSubscribing();
    }
}
