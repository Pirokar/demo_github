package im.threads.business.formatters;

import java.text.DateFormatSymbols;

/**
 * for date formatter in rus lang, with correct month suffix
 */
public final class RussianFormatSymbols extends DateFormatSymbols {
    private String[] russianMonth = {
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
    };

    public RussianFormatSymbols() {
        super();
        setMonths(russianMonth);
    }
}
