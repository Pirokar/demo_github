package im.threads.business.utils.gson

import android.net.Uri
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class UriDeserializer : JsonDeserializer<Uri?> {
    @Throws(JsonParseException::class)
    override fun deserialize(src: JsonElement, srcType: Type, context: JsonDeserializationContext): Uri {
        return Uri.parse(src.asString)
    }
}
