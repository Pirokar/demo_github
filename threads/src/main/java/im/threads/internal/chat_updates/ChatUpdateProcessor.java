package im.threads.internal.chat_updates;

import androidx.annotation.NonNull;

import java.util.List;

import im.threads.internal.formatters.ChatItemType;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ClientNotificationDisplayType;
import im.threads.internal.model.QuickReply;
import im.threads.internal.model.SpeechMessageUpdate;
import im.threads.internal.model.Survey;
import im.threads.internal.transport.ChatItemProviderData;
import im.threads.internal.transport.TransportException;
import im.threads.internal.transport.models.AttachmentSettings;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public class ChatUpdateProcessor {

    private static ChatUpdateProcessor instance;

    private final FlowableProcessor<String> typingProcessor = PublishProcessor.create();
    private final FlowableProcessor<AttachmentSettings> attachmentSettingsProcessor = PublishProcessor.create();
    private final FlowableProcessor<String> outgoingMessageReadProcessor = PublishProcessor.create();
    private final FlowableProcessor<String> incomingMessageReadProcessor = PublishProcessor.create();
    private final FlowableProcessor<ChatItem> newMessageProcessor = PublishProcessor.create();
    private final FlowableProcessor<ChatItemProviderData> messageSendSuccessProcessor = PublishProcessor.create();
    private final FlowableProcessor<String> messageSendErrorProcessor = PublishProcessor.create();
    private final FlowableProcessor<ChatItemType> removeChatItemProcessor = PublishProcessor.create();
    private final FlowableProcessor<Survey> surveySendSuccessProcessor = PublishProcessor.create();
    private final FlowableProcessor<String> deviceAddressChangedProcessor = PublishProcessor.create();
    private final FlowableProcessor<Boolean> userInputEnableProcessor = PublishProcessor.create();
    private final FlowableProcessor<List<QuickReply>> quickRepliesProcessor = PublishProcessor.create();
    private final FlowableProcessor<ClientNotificationDisplayType> clientNotificationDisplayTypeProcessor = PublishProcessor.create();
    private final FlowableProcessor<SpeechMessageUpdate> speechMessageUpdateProcessor = PublishProcessor.create();

    private final FlowableProcessor<TransportException> errorProcessor = PublishProcessor.create();

    public static ChatUpdateProcessor getInstance() {
        if (instance == null) {
            instance = new ChatUpdateProcessor();
        }
        return instance;
    }

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

    public void postChatItemSendSuccess(@NonNull ChatItemProviderData chatItemProviderData) {
        messageSendSuccessProcessor.onNext(chatItemProviderData);
    }

    public void postChatItemSendError(@NonNull String uuid) {
        messageSendErrorProcessor.onNext(uuid);
    }

    public void postRemoveChatItem(@NonNull ChatItemType chatItemType) {
        removeChatItemProcessor.onNext(chatItemType);
    }

    public void postSurveySendSuccess(Survey survey) {
        surveySendSuccessProcessor.onNext(survey);
    }

    public void postDeviceAddressChanged(String deviceAddress) {
        deviceAddressChangedProcessor.onNext(deviceAddress);
    }

    public void postUserInputEnableChanged(Boolean enable) {
        userInputEnableProcessor.onNext(enable);
    }

    public void postQuickRepliesChanged(List<QuickReply> quickReplies) {
        quickRepliesProcessor.onNext(quickReplies);
    }

    public void postClientNotificationDisplayType(ClientNotificationDisplayType type) {
        clientNotificationDisplayTypeProcessor.onNext(type);
    }

    public void postError(@NonNull TransportException error) {
        errorProcessor.onNext(error);
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

    public FlowableProcessor<ChatItemProviderData> getMessageSendSuccessProcessor() {
        return messageSendSuccessProcessor;
    }

    public FlowableProcessor<String> getMessageSendErrorProcessor() {
        return messageSendErrorProcessor;
    }

    public FlowableProcessor<ChatItemType> getRemoveChatItemProcessor() {
        return removeChatItemProcessor;
    }

    public FlowableProcessor<Survey> getSurveySendSuccessProcessor() {
        return surveySendSuccessProcessor;
    }

    public FlowableProcessor<String> getDeviceAddressChangedProcessor() {
        return deviceAddressChangedProcessor;
    }

    public FlowableProcessor<Boolean> getUserInputEnableProcessor() {
        return userInputEnableProcessor;
    }

    public FlowableProcessor<List<QuickReply>> getQuickRepliesProcessor() {
        return quickRepliesProcessor;
    }

    public FlowableProcessor<ClientNotificationDisplayType> getClientNotificationDisplayTypeProcessor() {
        return clientNotificationDisplayTypeProcessor;
    }

    public FlowableProcessor<TransportException> getErrorProcessor() {
        return errorProcessor;
    }
}