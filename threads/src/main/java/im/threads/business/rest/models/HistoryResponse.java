package im.threads.business.rest.models;

import androidx.annotation.Nullable;

import java.util.List;

import im.threads.business.models.ConsultInfo;
import im.threads.business.models.MessageFromHistory;
import im.threads.business.transport.models.Operator;

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
            Operator operator = agentInfo.getAgent();
            if (operator != null) {
                return new ConsultInfo(
                        operator.getAliasOrName(),
                        String.valueOf(operator.getId()),
                        operator.getStatus(),
                        operator.getOrganizationUnit(),
                        operator.getPhotoUrl(),
                        operator.getRole()
                );
            }
        }
        return null;
    }

    public List<MessageFromHistory> getMessages() {
        return messages;
    }

    private class AgentInfo {
        Operator agent;

        public Operator getAgent() {
            return agent;
        }
    }
}
