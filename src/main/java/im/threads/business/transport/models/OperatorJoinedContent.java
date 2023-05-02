package im.threads.business.transport.models;

public final class OperatorJoinedContent {
    private long id;
    private String type;
    private String uuid;
    private String title;
    private Operator operator;
    private boolean display;
    private String text;
    private long threadId;

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

    public boolean isDisplay() {
        return display;
    }

    public String getText() {
        return text;
    }

    public long getThreadId() {
        return threadId;
    }
}