package edna.chatcenter.demo.appCode.push

import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException
import edna.chatcenter.demo.integrationCode.EdnaChatCenterApplication
import java.io.IOException

object HCMTokenRefresher {
    fun requestToken(application: EdnaChatCenterApplication) {
        val hcmAppId = AGConnectOptionsBuilder().build(application).getString("client/app_id")
        if (hcmAppId != null) {
            try {
                val hmsInstanceId = HmsInstanceId.getInstance(application)
                val token = hmsInstanceId.getToken(hcmAppId, "HCM")
                application.chatCenterUI?.setFcmToken(token)
            } catch (e: IOException) {
            } catch (e: ApiException) {
            }
        }
    }
}
