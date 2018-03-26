package im.threads.model;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.List;

/**
 * Created by Vit on 23.06.2017.
 * ответ на запрос истории v2
 * в структуре появилась информация об операторе
 */

public class HistoryResponse {

    private List<MessgeFromHistory> messages;
    private AgentInfo agentInfo;

    public HistoryResponse(List<MessgeFromHistory> messages) {
        this.messages = messages;
    }

    public ConsultInfo getConsultInfo() {
        return agentInfo != null ? agentInfo.getAgent() : null;
    }

    public List<MessgeFromHistory> getMessages() {
        return messages;
    }

    public static HistoryResponse getHistoryFromServerResponse(String response) {
        HistoryResponse historyResponse = null;
        try {
            if (response != null) {
                historyResponse = new Gson().fromJson(response, HistoryResponse.class);
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }

        return historyResponse;
    }

    private class AgentInfo {
        ConsultInfo agent;

        public ConsultInfo getAgent() {
            return agent;
        }
    }
}
