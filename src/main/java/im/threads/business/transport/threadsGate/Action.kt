package im.threads.business.transport.threadsGate

import com.google.gson.annotations.SerializedName

enum class Action {
    @SerializedName("registerDevice")
    REGISTER_DEVICE,

    @SerializedName("sendMessage")
    SEND_MESSAGE,

    @SerializedName("getMessages")
    GET_MESSAGES,

    @SerializedName("getStatuses")
    GET_STATUSES,

    @SerializedName("updateStatuses")
    UPDATE_STATUSES,

    UNDEFINED
}
