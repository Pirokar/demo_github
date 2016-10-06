package com.sequenia.threads.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by yuri on 06.10.2016.
 */

public class ConsultPhrasesCash {
    private static Set<String> cash = new HashSet<>();
    private static ConsultPhrasesCash instance;

    private ConsultPhrasesCash() {
    }

    public static ConsultPhrasesCash getInstance() {
        if (instance == null) instance = new ConsultPhrasesCash();
        return instance;
    }
    public void add(String phrase){
        cash.add(phrase);
    }
    public boolean contains(String phrase){
        return cash.contains(phrase);
    }
}
