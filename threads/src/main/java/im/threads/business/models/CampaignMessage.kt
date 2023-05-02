package im.threads.business.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class CampaignMessage(
    val text: String,
    val senderName: String,
    val receivedDate: Date,
    val chatMessageId: String,
    val gateMessageId: Long,
    val expiredAt: Date,
    val skillId: Int,
    val campaign: String,
    val priority: Int
) : Parcelable

const val CAMPAIGN_DATE_FORMAT_PARSE = "yyyy-MM-dd'T'HH:mm:ssZ"
const val CAMPAIGN_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
