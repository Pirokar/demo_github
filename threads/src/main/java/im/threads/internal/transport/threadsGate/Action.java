package im.threads.internal.transport.threadsGate;

import com.google.gson.annotations.SerializedName;

public enum Action {
    @SerializedName("registerDevice")
    REGISTER_DEVICE,
    @SerializedName("sendMessage")
    SEND_MESSAGE,
    @SerializedName("getMessages")
    GET_MESSAGES,
    @SerializedName("getStatuses")
    GET_STATUSES,
    @SerializedName("updateStatuses")
    UPDATE_STATUSES;
}
