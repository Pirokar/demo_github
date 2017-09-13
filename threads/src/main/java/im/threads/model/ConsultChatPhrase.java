package im.threads.model;

import android.content.Context;
import android.text.TextUtils;

import im.threads.utils.PrefUtils;

public class ConsultChatPhrase {
    private String avatarPath;
    protected String consultId;

    public ConsultChatPhrase(final String avatarPath, final String consultId) {
        this.avatarPath = avatarPath;
        this.consultId = consultId;
    }

    public String getConsultId() {
        return consultId;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(Context context, String avatarPath) {
        if (!TextUtils.isEmpty(avatarPath)) {
            if (avatarPath.startsWith("http")) {
                this.avatarPath = avatarPath;
            } else {
                this.avatarPath = PrefUtils.getServerUrlMetaInfo(context) + "files/" + avatarPath;
            }
        }
    }

}
