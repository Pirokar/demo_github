package im.threads.business.formatters

import java.text.DateFormatSymbols

/**
 * for date formatter in rus lang, with correct month suffix
 */
class RussianFormatSymbols : DateFormatSymbols() {
    private val russianMonth = arrayOf(
        "января",
        "февраля",
        "марта",
        "апреля",
        "мая",
        "июня",
        "июля",
        "августа",
        "сентября",
        "октября",
        "ноября",
        "декабря"
    )

    init {
        months = russianMonth
    }
}
