package com.sequenia.threads.holders;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sequenia.threads.R;
import com.sequenia.threads.model.ChatStyle;
import com.sequenia.threads.model.UnreadMessages;

import static com.sequenia.threads.model.ChatStyle.INVALID;

/**
 * Created by yuri on 24.08.2016.
 */
public class UnreadMessageViewHolder extends RecyclerView.ViewHolder {
    private TextView mTextView;
    private static ChatStyle style;

    public UnreadMessageViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unread_counter, parent, false));
        mTextView = (TextView) itemView.findViewById(R.id.text);
        if (null != style && style.connectionMessageTextColor != INVALID) {
            mTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), style.connectionMessageTextColor));
        }
    }

    public void onBind(UnreadMessages unreadMessages) {
        mTextView.setText(unreadMessages.getMessage());
    }

}
