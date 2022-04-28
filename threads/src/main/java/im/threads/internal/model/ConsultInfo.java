package im.threads.internal.model;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;

public final class ConsultInfo {

    private final String name;
    private final String id;
    private final String status;
    private final String organizationUnit;
    private final String photoUrl;
    private final String role;

    @Deprecated
    public ConsultInfo(String name,
                       String id,
                       String status,
                       String organizationUnit,
                       String photoUrl) {
        this.name = name;
        this.id = id;
        this.status = status;
        this.organizationUnit = organizationUnit;
        this.photoUrl = photoUrl;
        this.role = null;
    }

    public ConsultInfo(String name,
                       String id,
                       String status,
                       String organizationUnit,
                       String photoUrl,
                       String role) {
        this.name = name;
        this.id = id;
        this.status = status;
        this.organizationUnit = organizationUnit;
        this.photoUrl = photoUrl;
        this.role = role;
    }

    @NonNull
    @Override
    public String toString() {
        return "ConsultInfo{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", status='" + status + '\'' +
                ", organizationUnit='" + organizationUnit + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", role='" + role + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getOrganizationUnit() {
        return organizationUnit;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getRole() {
        return role;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", name);
        jsonObject.addProperty("status", status);
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("photoUrl", photoUrl);
        jsonObject.addProperty("role", role);
        return jsonObject;
    }
}
