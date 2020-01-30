package im.threads.internal.transport.threads_gate;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import im.threads.internal.model.ChatItem;
import im.threads.internal.transport.MessageParser;
import im.threads.internal.transport.threads_gate.responses.BaseMessage;

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

    static boolean checkId(@NonNull final BaseMessage message, final String currentClientId) {
        return MessageParser.checkId(message.getContent(), currentClientId);
    }
}
