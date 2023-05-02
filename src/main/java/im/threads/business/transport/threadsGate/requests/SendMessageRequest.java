package im.threads.business.transport.threadsGate.requests;

import com.google.gson.JsonObject;

import im.threads.business.transport.threadsGate.Action;

public final class SendMessageRequest extends BaseRequest<SendMessageRequest.Data> {

    public SendMessageRequest(String correlationId, Data data) {
        super(Action.SEND_MESSAGE, correlationId, data);
    }

    public static final class Data {

        private final String deviceAddress;

        private final String messageId;

        private final JsonObject content;

        private final boolean important;

        public Data(String deviceAddress,
                    String messageId,
                    JsonObject content,
                    boolean important) {
            this.deviceAddress = deviceAddress;
            this.messageId = messageId;
            this.content = content;
            this.important = important;
        }
    }
}
