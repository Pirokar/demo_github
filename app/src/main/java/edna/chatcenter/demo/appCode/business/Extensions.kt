package edna.chatcenter.demo.appCode.business

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

inline fun <reified T : Any> T.ordinal(): Int {
    if (T::class.isSealed) {
        return T::class.java.classes.indexOfFirst { sub -> sub == javaClass }
    }
    val klass = if (T::class.isCompanion) {
        javaClass.declaringClass
    } else {
        javaClass
    }

    return klass.superclass?.classes?.indexOfFirst { it == klass } ?: -1
}

/**
 * Конвертирует JSON внутри строки в Map<String, String>.
 * Внутри должен быть линейный JSON, без объектов и массивов
 */
fun String?.jsonStringToMap(): Map<String, String> {
    return if (this.isNullOrBlank()) {
        mapOf()
    } else {
        val gson = Gson()
        val type = object : TypeToken<Map<String, String>>() {}.type
        gson.fromJson(this, type)
    }
}
