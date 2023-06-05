package im.threads.business.models

import com.google.gson.annotations.SerializedName
import im.threads.business.logger.LoggerEdna

enum class MessageStatus(val value: Int) {
    SENDING(0),

    FAILED(1),

    @SerializedName("sent")
    SENT(2),

    @SerializedName("enqueued")
    ENQUEUED(3),

    @SerializedName("delivered")
    DELIVERED(4),

    @SerializedName("read")
    READ(5);

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
                "enqueued" -> ENQUEUED
                "read" -> READ
                "failed" -> FAILED
                else -> SENDING
            }
        }
    }
}
