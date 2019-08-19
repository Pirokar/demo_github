package im.threads.internal.formatters;

import java.text.DateFormatSymbols;

/**
 * for date formatter in rus lang, with correct month suffix
 */
public class RussianFormatSymbols extends DateFormatSymbols {
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
