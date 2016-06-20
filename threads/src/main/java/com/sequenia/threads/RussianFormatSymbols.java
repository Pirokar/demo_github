package com.sequenia.threads;

import java.text.DateFormatSymbols;

/**
 * Created by yuri on 08.06.2016.
 *for date formatter in rus lang, with correct month suffix
 */
public class RussianFormatSymbols extends DateFormatSymbols {
    String[] russianMonth = {
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
