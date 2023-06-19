package im.threads.business.transport.threadsGate.responses;

import com.google.gson.JsonObject;

import im.threads.business.transport.threadsGate.Action;

public class BaseResponse {
    private Action action;
    private String correlationId;
    private JsonObject data;

    public Action getAction() {
        return action;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public JsonObject getData() {
        return data;
    }
}
