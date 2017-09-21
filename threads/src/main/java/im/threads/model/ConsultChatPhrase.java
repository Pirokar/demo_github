package im.threads.model;

import android.text.TextUtils;

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

    public boolean hasAvatar() {
        return !TextUtils.isEmpty(avatarPath);
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
}
