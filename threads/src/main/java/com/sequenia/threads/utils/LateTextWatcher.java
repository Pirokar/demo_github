package com.sequenia.threads.utils;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by yuri on 09.09.2016.
 */
public abstract class LateTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public abstract void afterTextChanged(Editable s);
}

