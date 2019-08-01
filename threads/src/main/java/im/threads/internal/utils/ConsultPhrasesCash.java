package im.threads.internal.utils;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by yuri on 06.10.2016.
 */

public class ConsultPhrasesCash {
    private static ConsultPhrasesCash instance;
    private static Set<String> cashWithNewLine = new TreeSet<>();
    private static Set<String> cashWithoutNewLine = new TreeSet<>();
    public static final int NOT_FOUND = -1;
    public static final int FOUND_WITH_NEW_LINE = 1;
    public static final int FOUND_WITHOUT_NEW_LINE = 2;
    private ConsultPhrasesCash() {
    }

    public static ConsultPhrasesCash getInstance() {
        if (instance == null) instance = new ConsultPhrasesCash();
        return instance;
    }
    public void addWithNewLine(String phrase) {
        cashWithNewLine.add(phrase);
    }

    public void addWithoutNewLine(String phrase) {
        cashWithoutNewLine.add(phrase);
    }

    public int contains(String phrase) {
        if (cashWithNewLine.contains(phrase))return FOUND_WITH_NEW_LINE;
        if (cashWithoutNewLine.contains(phrase))return FOUND_WITHOUT_NEW_LINE;
        return NOT_FOUND;
    }
}
