package im.threads.business.models;

import androidx.annotation.NonNull;

public enum ClientNotificationDisplayType {
    ALL,
    CURRENT_THREAD_ONLY,
    CURRENT_THREAD_WITH_GROUPING;

    @NonNull
    public static ClientNotificationDisplayType fromString(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException ex) {
            return ClientNotificationDisplayType.CURRENT_THREAD_ONLY;
        }
    }
}
