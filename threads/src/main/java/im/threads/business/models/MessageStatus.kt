package im.threads.business.models;

import com.google.gson.annotations.SerializedName;

public enum MessageStatus {
    SENDING,
    @SerializedName("delivered") DELIVERED,
    @SerializedName("sent") SENT,
    @SerializedName("read") READ,
    FAILED;

    public static MessageStatus fromOrdinal(int ordinal) {
        return MessageStatus.values()[ordinal];
    }

}
