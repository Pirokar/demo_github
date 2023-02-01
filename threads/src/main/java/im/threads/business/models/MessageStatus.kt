package im.threads.business.models

import com.google.gson.annotations.SerializedName

enum class MessageStatus {
    SENDING,
    @SerializedName("delivered") DELIVERED,
    @SerializedName("sent") SENT,
    @SerializedName("read") READ,
    FAILED;

    companion object {
        @JvmStatic
        fun fromOrdinal(ordinal: Int): MessageStatus {
            return values()[ordinal]
        }

        @JvmStatic
        fun fromString(string: String?): MessageStatus? {
            if (string == null) return null

            return when (string) {
                "delivered" -> DELIVERED
                "sent" -> SENT
                "read" -> READ
                "failed" -> FAILED
                else -> SENDING
            }
        }
    }
}