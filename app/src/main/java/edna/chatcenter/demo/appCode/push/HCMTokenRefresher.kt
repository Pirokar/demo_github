package edna.chatcenter.demo.appCode.push

import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException
import edna.chatcenter.demo.integrationCode.EdnaChatCenterApplication
import edna.chatcenter.ui.core.logger.LoggerEdna
import java.io.IOException

object HCMTokenRefresher {
    fun requestToken(application: EdnaChatCenterApplication) {
        val hcmAppId = AGConnectOptionsBuilder().build(application).getString("client/app_id")
        if (hcmAppId != null) {
            try {
                val hmsInstanceId = HmsInstanceId.getInstance(application)
                val token = hmsInstanceId.getToken(hcmAppId, "HCM")
                application.chatCenterUI?.setHcmToken(token)
            } catch (e: IOException) {
                LoggerEdna.info("Called requestToken()  Exception 1: $e")
            } catch (e: ApiException) {
                LoggerEdna.info("Called requestToken()  Exception 2: $e")
            }
        }
    }
}
