package im.threads.internal.transport.threads_gate.requests;

import java.util.List;

import im.threads.internal.transport.threads_gate.Action;
import im.threads.internal.transport.threads_gate.MessageStatus;

public final class UpdateStatusesRequest extends BaseRequest<UpdateStatusesRequest.Data> {

    public UpdateStatusesRequest(String correlationId, Data data) {
        super(Action.UPDATE_STATUSES, correlationId, data);
    }

    public static final class Data {

        private final String deviceAddress;

        private final List<MessageStatusData> statuses;

        public Data(String deviceAddress, List<MessageStatusData> statuses) {
            this.deviceAddress = deviceAddress;
            this.statuses = statuses;
        }
    }

    public static final class MessageStatusData {

        private final String messageId;

        private final MessageStatus status;

        public MessageStatusData(String messageId, MessageStatus status) {
            this.messageId = messageId;
            this.status = status;
        }
    }
}
