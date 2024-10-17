package edna.chatcenter.demo.appCode.push

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Base64
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import edna.chatcenter.demo.integrationCode.EdnaChatCenterApplication
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

class CustomPushHcmIntentService : HmsMessageService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val application = applicationContext as EdnaChatCenterApplication
        application.chatCenterUI?.setHcmToken(token)
    }

    @SuppressLint("RestrictedApi")
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val application = applicationContext as EdnaChatCenterApplication
        application.chatCenterUI?.handlePushMessage(base64JsonStringToBundle(message.data))
    }

    private fun base64JsonStringToBundle(base64Str: String): Bundle {
        return try {
            val data: ByteArray = Base64.decode(base64Str, 0)
            val decodedStr = String(data)
            jsonObjectToBundle(JSONObject(decodedStr))
        } catch (exception: JSONException) {
            Bundle()
        } catch (exception: UnsupportedEncodingException) {
            Bundle()
        }
    }

    @Throws(JSONException::class)
    private fun jsonObjectToBundle(jsonObject: JSONObject): Bundle {
        val bundle = Bundle()
        val iterator: Iterator<*> = jsonObject.keys()
        while (iterator.hasNext()) {
            val key = iterator.next() as String
            val value = jsonObject.getString(key)
            bundle.putString(key, value)
        }
        return bundle
    }
}
