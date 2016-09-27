package com.sequenia.threads.formatters;

import java.util.Locale;

/**
 * Created by yuri on 14.09.2016.
 */
public class ImagesPlurals {//coz bugs in russian plurals
    private Locale locale;

    public ImagesPlurals(Locale locale) {
        this.locale = locale;
    }

    String getForQuantity(int quantity) {
        if (locale.getLanguage().equalsIgnoreCase("ru")) {
            if (quantity == 1) {
                return "Изображение";
            }
            if ((quantity % 10 == 1) && quantity != 11) return "изображение";
            else if ((quantity > 1 && quantity < 5)
                    || (quantity > 20 && (quantity % 10 == 2 || quantity % 10 == 3 || quantity % 10 == 4)))
                return "изображения";
            else return "изображений";

        } else {
            if (quantity == 1) return "image";
            else return "images";
        }
    }
}
