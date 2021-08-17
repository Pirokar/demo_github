package im.threads.internal.model

import android.os.Parcelable
import java.util.Date
import kotlinx.parcelize.Parcelize

@Parcelize
data class CampaignMessage(
    val text: String,
    val receivedDate: Date,
    val gateMessageId: Long,
    val expiredAt: Date,
    val skillId: Int,
    val campaign: String,
    val priority: Int
) : Parcelable

const val CAMPAIGN_DATE_FORMAT_PARSE = "yyyy-MM-dd'T'HH:mm:ssZ"
const val CAMPAIGN_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
