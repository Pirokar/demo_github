package com.sequenia.threads.holders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.sequenia.threads.R;

/**
 * Created by yuri on 10.06.2016.
 */
public class SearchingConsultViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "SearchingViewHolder ";

    public SearchingConsultViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_searching_consult, parent, false));
    }
}
