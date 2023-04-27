package im.threads.business.models.enums

enum class CurrentUiTheme(val value: Int) {
    SYSTEM(0), LIGHT(1), DARK(2);

    companion object {
        fun fromInt(intValue: Int?) = values().firstOrNull { it.ordinal == intValue } ?: SYSTEM
    }
}
