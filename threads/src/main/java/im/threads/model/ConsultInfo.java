package im.threads.model;

import org.json.JSONException;
import org.json.JSONObject;

import im.threads.internal.ThreadsLogger;

public class ConsultInfo {

    private static final String TAG = ConsultInfo.class.getSimpleName();

    private final String name;
    private final String id;
    private final String status;
    private final String organizationUnit;
    private final String photoUrl;

    public ConsultInfo(String name, String id, String status, String organizationUnit, String photoUrl) {
        this.name = name;
        this.id = id;
        this.status = status;
        this.organizationUnit = organizationUnit;
        this.photoUrl = photoUrl;
    }

    @Override
    public String toString() {
        return "ConsultInfo{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", status='" + status + '\'' +
                ", organizationUnit='" + organizationUnit + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
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

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name);
            jsonObject.put("status", status);
            jsonObject.put("id", id);
            jsonObject.put("photoUrl", photoUrl);
        } catch (JSONException e) {
            ThreadsLogger.e(TAG, "toJson", e);
        }
        return jsonObject;
    }
}
