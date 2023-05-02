package im.threads.business.transport.threadsGate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import im.threads.business.models.ChatItem;
import im.threads.business.transport.MessageParser;
import im.threads.business.transport.threadsGate.responses.BaseMessage;

final class ThreadsGateMessageParser {

    private ThreadsGateMessageParser() {
    }

    static String getType(@NonNull final BaseMessage message) {
        return MessageParser.getType(message.getContent());
    }

    @Nullable
    static ChatItem format(@NonNull final BaseMessage message) {
        return MessageParser.format(message.getMessageId(), message.getSentAt().getTime(), message.getNotification(), message.getContent());
    }
}
