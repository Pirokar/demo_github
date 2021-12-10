package im.threads.internal.transport

import android.content.Context
import com.edna.android.push_lite.fcm.FcmPushService
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException
import im.threads.internal.utils.PrefUtils
import im.threads.internal.utils.ThreadsLogger
import java.io.IOException

object HCMTokenRefresher {

    fun collectTokenIfNeeded(context: Context) {
        val cloudMessagingType = PrefUtils.getCloudMessagingType()
        val hcmAppId = AGConnectServicesConfig.fromContext(context).getString("client/app_id")
        if (cloudMessagingType == null) {
            if (FcmPushService.newToken != null) {
                PrefUtils.setFcmToken(FcmPushService.newToken)
            } else if (hcmAppId != null) {
                try {
                    val token = requestTokenFromHcm(context, hcmAppId)
                    ThreadsLogger.e("ApplicationConfig", "token received: $token")
                    PrefUtils.setHcmToken(token)
                } catch (e: IOException) {
                    ThreadsLogger.e("ApplicationConfig", "failed to request token", e)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun requestTokenFromHcm(context: Context, hcmAppId: String): String? {
        val hmsInstanceId = HmsInstanceId.getInstance(context)
        ThreadsLogger.i("ApplicationConfig", "Current HCM HmsInstanceId: $hmsInstanceId")
        return try {
            hmsInstanceId.getToken(hcmAppId, "HCM")
        } catch (e: ApiException) {
            throw IOException(e.cause)
        }
    }
}
