package im.threads;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class UserInfoBuilder {
    @NonNull
    String clientId;
    @Nullable
    String clientIdSignature = null;
    @Nullable
    String userName = null;
    @Nullable
    String data = null;
    @Nullable
    String appMarker = null;

    /**
     * true if client id is encrypted
     */
    boolean clientIdEncrypted = false;

    public UserInfoBuilder(@NonNull String clientId) {
        if (TextUtils.isEmpty(clientId)) {
            throw new IllegalArgumentException("clientId must not be empty");
        }
        this.clientId = clientId;
    }

    public UserInfoBuilder setClientIdSignature(String clientIdSignature) {
        this.clientIdSignature = clientIdSignature;
        return this;
    }

    public UserInfoBuilder setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    /**
     * Any additional information can be provided in data string, i.e. "{balance:"1000.00", fio:"Vasya Pupkin"}"
     */
    public UserInfoBuilder setData(String data) {
        this.data = data;
        return this;
    }

    public UserInfoBuilder setAppMarker(String appMarker) {
        this.appMarker = appMarker;
        return this;
    }

    public UserInfoBuilder setClientIdEncrypted(boolean clientIdEncrypted) {
        this.clientIdEncrypted = clientIdEncrypted;
        return this;
    }
}
