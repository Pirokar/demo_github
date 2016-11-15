package com.sequenia.threads.holders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sequenia.threads.R;
import com.sequenia.threads.model.ChatStyle;
import com.sequenia.threads.utils.PrefUtils;
import com.sequenia.threads.views.ViewTypingInProgress;

import static com.sequenia.threads.model.ChatStyle.INVALID;

/**
 * Created by yuri on 09.06.2016.
 * layout/item_consult_typing.xml
 */
public class ConsultIsTypingViewHolder extends RecyclerView.ViewHolder {
    public ImageView mConsultImageView;
    public ViewTypingInProgress mViewTypingInProgress;
    private ChatStyle style;

    public ConsultIsTypingViewHolder(ViewGroup parent) {
        super((LayoutInflater.from(parent.getContext())).inflate(R.layout.item_consult_typing, parent, false));
        mConsultImageView = (ImageView) itemView.findViewById(R.id.image);
        mViewTypingInProgress = (ViewTypingInProgress) itemView.findViewById(R.id.typing_in_progress);
        if (style == null) style = PrefUtils.getIncomingStyle(itemView.getContext());
        if (null != style) {
            if (style.chatToolbarTextColorResId!=INVALID){
                mViewTypingInProgress.setColor(style.chatToolbarTextColorResId);
            }
        }
    }

    public void onBind(View.OnClickListener consultClickListener) {
        mConsultImageView.setOnClickListener(consultClickListener);
    }

    public void stopTyping() {
        ((ViewTypingInProgress) itemView.findViewById(R.id.typing_in_progress))
                .removeAnimation();
    }

    public void beginTyping() {
        ((ViewTypingInProgress) itemView.findViewById(R.id.typing_in_progress)
        ).animateViews();
    }
}
