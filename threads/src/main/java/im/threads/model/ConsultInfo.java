package im.threads.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yuri on 09.09.2016.
 *
 */
public class ConsultInfo {
    private final String name;
    private final String id;
    private final String status;
    private final String photoUrl;

    public ConsultInfo(String name, String id, String status, String photoUrl) {
        this.name = name;
        this.id = id;
        this.status = status;
        this.photoUrl = photoUrl;
    }

    @Override
    public String toString() {
        return "ConsultInfo{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", status='" + status + '\'' +
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
            e.printStackTrace();
        }
        return jsonObject;
    }
}
