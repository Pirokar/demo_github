package im.threads.internal.transport.threads_gate.requests;

import im.threads.internal.transport.threads_gate.Action;

public final class GetMessagesRequest extends BaseRequest<GetMessagesRequest.Data> {

    public GetMessagesRequest(String correlationId, Data data) {
        super(Action.GET_MESSAGES, correlationId, data);
    }

    public static final class Data {

        private final String deviceAddress;
        private final String startMessageId;
        private final int maxCount;
        private final int offset;

        public Data(String deviceAddress, String startMessageId, int maxCount, int offset) {
            this.deviceAddress = deviceAddress;
            this.startMessageId = startMessageId;
            this.maxCount = maxCount;
            this.offset = offset;
        }
    }
}
