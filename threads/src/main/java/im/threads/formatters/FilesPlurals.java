package im.threads.formatters;

import java.util.Locale;

/**
 * Created by yuri on 14.09.2016.
 */
public class FilesPlurals {
    private Locale locale;

    public FilesPlurals(Locale locale) {
        this.locale = locale;
    }

    String getForQuantity(int quantity) {
        if (locale.getLanguage().equalsIgnoreCase("ru")) {
            if (quantity == 1 || (quantity % 10 == 1) && quantity != 11) return "файл";
            else if ((quantity > 1 && quantity < 5)
                    || (quantity > 20 && (quantity % 10 == 2 || quantity % 10 == 3 || quantity % 10 == 4)))
                return "файла";
            else return "файлов";

        } else {
            if (quantity == 1) return "file";
            else return "files";
        }
    }
}
