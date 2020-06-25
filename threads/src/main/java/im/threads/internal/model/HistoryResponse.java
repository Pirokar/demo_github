package im.threads.internal.model;

import androidx.annotation.Nullable;

import java.util.List;

/**
 * ответ на запрос истории v2
 * в структуре появилась информация об операторе
 */
public final class HistoryResponse {

    private List<MessageFromHistory> messages;
    private AgentInfo agentInfo;

    public HistoryResponse(List<MessageFromHistory> messages) {
        this.messages = messages;
    }

    @Nullable
    public ConsultInfo getConsultInfo() {
        if (agentInfo != null && agentInfo.getAgent() != null) {
            im.threads.internal.transport.models.Operator operator = agentInfo.getAgent();
            if (operator != null) {
                return new ConsultInfo(
                        operator.getAliasOrName(),
                        String.valueOf(operator.getId()),
                        operator.getStatus(),
                        operator.getOrganizationUnit(),
                        operator.getPhotoUrl()
                );
            }
        }
        return null;
    }

    public List<MessageFromHistory> getMessages() {
        return messages;
    }

    private class AgentInfo {
        im.threads.internal.transport.models.Operator agent;

        public im.threads.internal.transport.models.Operator getAgent() {
            return agent;
        }
    }
}
