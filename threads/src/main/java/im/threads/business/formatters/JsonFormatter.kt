package im.threads.business.formatters

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser

class JsonFormatter {
    fun jsonToPrettyFormat(jsonString: String?): String {
        return try {
            val jsonObject = JsonParser.parseString(jsonString)
            val gson: Gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()
            gson.toJson(jsonObject)
        } catch (exc: Exception) {
            "Cannot create PrettyJson. Input json: $jsonString"
        }
    }
}
