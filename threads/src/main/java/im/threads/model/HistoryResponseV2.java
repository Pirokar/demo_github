package im.threads.model;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.List;

/**
 * Created by Vit on 23.06.2017.
 * ответ на запрос истории v2
 * в структуре появилась информация об операторе
 */

public class HistoryResponseV2 {

    private List<MessgeFromHistory> messages;
    private AgentInfo agentInfo;

    public ConsultInfo getConsultInfo() {
        return agentInfo != null ? agentInfo.getAgent() : null;
    }

    public List<MessgeFromHistory> getMessages() {
        return messages;
    }

    public static HistoryResponseV2 getHistoryFromServerResponse(String response) {
        HistoryResponseV2 historyResponseV2 = null;
        try {
            if (response != null) {
                historyResponseV2 = new Gson().fromJson(response, HistoryResponseV2.class);
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }

        return historyResponseV2;
    }

    private class AgentInfo {
        ConsultInfo agent;

        public ConsultInfo getAgent() {
            return agent;
        }
    }
}
