package im.threads.internal.transport.threads_gate;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import im.threads.internal.model.ChatItem;
import im.threads.internal.transport.MessageParser;
import im.threads.internal.transport.threads_gate.responses.BaseMessage;

public final class ThreadsGateMessageParser {

    private ThreadsGateMessageParser() {
    }

    public static String getType(@NonNull final BaseMessage message) {
        return MessageParser.getType(message.getContent());
    }

    @Nullable
    public static ChatItem format(@NonNull final BaseMessage message) {
        return MessageParser.format(message.getMessageId(), message.getSentAt().getTime(), message.getNotification(), message.getContent());
    }
}
