package im.threads.internal.utils // ktlint-disable filename

fun String?.capitalize(): String {
    if (this.isNullOrBlank()) {
        return ""
    }
    val firstChar = this.first()
    return if (Character.isUpperCase(firstChar)) {
        this
    } else {
        return "${firstChar.uppercaseChar()}${this.substring(1)}"
    }
}
