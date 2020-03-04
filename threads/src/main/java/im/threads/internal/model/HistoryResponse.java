package im.threads.internal.model;

import android.support.annotation.Nullable;

import com.google.gson.JsonSyntaxException;

import java.util.List;

import im.threads.internal.Config;
import im.threads.internal.utils.ThreadsLogger;

/**
 * ответ на запрос истории v2
 * в структуре появилась информация об операторе
 */
public final class HistoryResponse {
    private static final String TAG = HistoryResponse.class.getSimpleName();

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

    public static HistoryResponse getHistoryFromServerResponse(String response) {
        HistoryResponse historyResponse = null;
        try {
            if (response != null) {
                historyResponse = Config.instance.gson.fromJson(response, HistoryResponse.class);
            }
        } catch (JsonSyntaxException e) {
            ThreadsLogger.e(TAG, "getHistoryFromServerResponse", e);
        }

        return historyResponse;
    }

    private class AgentInfo {
        im.threads.internal.transport.models.Operator agent;

        public im.threads.internal.transport.models.Operator getAgent() {
            return agent;
        }
    }
}
