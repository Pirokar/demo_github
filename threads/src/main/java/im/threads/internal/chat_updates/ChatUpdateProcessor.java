package im.threads.internal.chat_updates;

import android.support.annotation.NonNull;

import im.threads.internal.model.ChatItem;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public class ChatUpdateProcessor {

    private static ChatUpdateProcessor instance;

    private final FlowableProcessor<String> typingProcessor = PublishProcessor.create();
    private final FlowableProcessor<String> messageReadProcessor = PublishProcessor.create();
    private final FlowableProcessor<ChatItem> newMessageProcessor = PublishProcessor.create();

    public static ChatUpdateProcessor getInstance() {
        if (instance == null) {
            instance = new ChatUpdateProcessor();
        }
        return instance;
    }

    public void postTyping(@NonNull String clientId) {
        typingProcessor.onNext(clientId);
    }

    public void postMessageRead(@NonNull String messageId) {
        messageReadProcessor.onNext(messageId);
    }

    public void postNewMessage(@NonNull ChatItem chatItem) {
        newMessageProcessor.onNext(chatItem);
    }

    public FlowableProcessor<String> getTypingProcessor() {
        return typingProcessor;
    }

    public FlowableProcessor<String> getMessageReadProcessor() {
        return messageReadProcessor;
    }

    public FlowableProcessor<ChatItem> getNewMessageProcessor() {
        return newMessageProcessor;
    }
}
