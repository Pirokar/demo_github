package im.threads.business.models;

public final class Client {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String externalClientId;
    private String closedThreads;
    private String lastThreadTime;
    private String online;
    private Boolean blocked;
    private Boolean blockRequested;
    private String data;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getExternalClientId() {
        return externalClientId;
    }

    public void setExternalClientId(String externalClientId) {
        this.externalClientId = externalClientId;
    }

    public String getClosedThreads() {
        return closedThreads;
    }

    public void setClosedThreads(String closedThreads) {
        this.closedThreads = closedThreads;
    }

    public String getLastThreadTime() {
        return lastThreadTime;
    }

    public void setLastThreadTime(String lastThreadTime) {
        this.lastThreadTime = lastThreadTime;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public Boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

    public Boolean getBlockRequested() {
        return blockRequested;
    }

    public void setBlockRequested(Boolean blockRequested) {
        this.blockRequested = blockRequested;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
