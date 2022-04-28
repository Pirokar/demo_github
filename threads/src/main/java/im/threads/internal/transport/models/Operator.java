package im.threads.internal.transport.models;

import android.text.TextUtils;

import androidx.annotation.Nullable;

public final class Operator {
    private long id;
    private String name;
    private String alias;
    private String status;
    private String photoUrl;
    private String gender;
    private String organizationUnit;
    private String role;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public String getStatus() {
        return status;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getGender() {
        return gender;
    }

    public String getOrganizationUnit() {
        return organizationUnit;
    }

    public String getRole() {
        return role;
    }

    @Nullable
    public String getAliasOrName() {
        if (!TextUtils.isEmpty(alias)) {
            return alias;
        }
        return name;
    }
}
