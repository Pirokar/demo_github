package im.threads.internal.transport.models;

import java.util.List;

public final class OperatorJoinedContent {
    private long id;
    private String type;
    private String uuid;
    private String title;
    private Operator operator;
    private List<String> providerIds;
    private boolean display;

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getUuid() {
        return uuid;
    }

    public String getTitle() {
        return title;
    }

    public Operator getOperator() {
        return operator;
    }

    public List<String> getProviderIds() {
        return providerIds;
    }

    public boolean isDisplay() {
        return display;
    }
}
