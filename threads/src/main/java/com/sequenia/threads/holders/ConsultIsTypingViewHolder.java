package com.sequenia.threads.holders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sequenia.threads.R;
import com.sequenia.threads.views.ViewTypingInProgress;

/**
 * Created by yuri on 09.06.2016.
 * layout/item_consult_typing.xml
 */
public class ConsultIsTypingViewHolder extends RecyclerView.ViewHolder {
    public ImageView mConsultImageView;

    public ConsultIsTypingViewHolder(ViewGroup parent) {
        super((LayoutInflater.from(parent.getContext())).inflate(R.layout.item_consult_typing, parent, false));
        mConsultImageView = (ImageView) itemView.findViewById(R.id.image);
    }

    public ConsultIsTypingViewHolder(View itemView) {
        super(itemView);
        mConsultImageView = (ImageView) itemView.findViewById(R.id.image);
    }


    public void onBind(View.OnClickListener consultClickListener) {
        mConsultImageView.setOnClickListener(consultClickListener);
    }

    public void stopTyping() {
        ((ViewTypingInProgress) itemView.findViewById(R.id.typing_in_progress)).removeAnimation();
    }
    public void beginTyping(){
        ((ViewTypingInProgress) itemView.findViewById(R.id.typing_in_progress)).animateViews();
    }
}
