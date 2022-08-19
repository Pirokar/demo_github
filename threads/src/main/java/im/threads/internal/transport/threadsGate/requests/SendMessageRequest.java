package im.threads.internal.transport.threadsGate.requests;

import com.google.gson.JsonObject;

import im.threads.internal.transport.threadsGate.Action;

public final class SendMessageRequest extends BaseRequest<SendMessageRequest.Data> {

    public SendMessageRequest(String correlationId, Data data) {
        super(Action.SEND_MESSAGE, correlationId, data);
    }

    public static final class Data {

        private final String deviceAddress;

        private final JsonObject content;

        private final boolean important;

        public Data(String deviceAddress,
                    JsonObject content,
                    boolean important) {
            this.deviceAddress = deviceAddress;
            this.content = content;
            this.important = important;
        }
    }
}
