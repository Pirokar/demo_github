package com.sequenia.threads.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by yuri on 06.10.2016.
 */

public class UserPhrasesCash {
    private static Set<String> cash = new HashSet<>();
    private static UserPhrasesCash instance;

    private UserPhrasesCash() {
    }

    public static UserPhrasesCash getInstance() {
        if (instance == null) instance = new UserPhrasesCash();
        return instance;
    }
    public void add(String phrase){
        cash.add(phrase);
    }
    public boolean contains(String phrase){
        return cash.contains(phrase);
    }
}
