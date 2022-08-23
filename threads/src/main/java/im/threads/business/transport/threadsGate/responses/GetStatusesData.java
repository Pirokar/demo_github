package im.threads.business.transport.threadsGate.responses;

import java.util.List;

import im.threads.business.transport.threadsGate.MessageStatus;

public class GetStatusesData {

    private List<Status> statuses;

    public List<Status> getStatuses() {
        return statuses;
    }

    public static final class Status {
        private String messageId;
        private MessageStatus status;

        public String getMessageId() {
            return messageId;
        }

        public MessageStatus getStatus() {
            return status;
        }
    }
}
