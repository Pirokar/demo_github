package im.threads.business.utils

fun String.paddingStart(paddingCount: Int, delimiter: String = " "): String {
    val stringBuilder = StringBuilder()
    for (i in 0 until paddingCount) {
        stringBuilder.append(delimiter)
    }
    stringBuilder.append(this)

    return stringBuilder.toString()
}

fun String.paddingEnd(paddingCount: Int, delimiter: String = " "): String {
    val stringBuilder = StringBuilder().also {
        it.append(this)
    }
    for (i in 0 until paddingCount) {
        stringBuilder.append(delimiter)
    }

    return stringBuilder.toString()
}

fun String.paddingStartEnd(
    paddingStartCount: Int,
    paddingEndCount: Int,
    delimiterStart: String = " ",
    delimiterEnd: String = " "
): String {
    this.paddingStart(paddingStartCount, delimiterStart).also {
        return it.paddingEnd(paddingEndCount, delimiterEnd)
    }
}