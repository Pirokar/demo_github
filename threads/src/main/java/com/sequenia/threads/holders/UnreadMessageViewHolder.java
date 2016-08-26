package com.sequenia.threads.holders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sequenia.threads.R;
import com.sequenia.threads.model.UnreadMessages;

/**
 * Created by yuri on 24.08.2016.
 */
public class UnreadMessageViewHolder extends RecyclerView.ViewHolder {
    private TextView mTextView;

    public UnreadMessageViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unread_counter, parent, false));
        mTextView = (TextView) itemView.findViewById(R.id.text);
    }

    public void onBind(UnreadMessages unreadMessages) {
        mTextView.setText(unreadMessages.getMessage());
    }

}
