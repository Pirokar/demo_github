package im.threads.holders;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import im.threads.R;
import im.threads.model.ChatStyle;
import im.threads.utils.PrefUtils;
import im.threads.views.ViewTypingInProgress;

/**
 * Created by yuri on 09.06.2016.
 * layout/item_consult_typing.xml
 */
public class ConsultIsTypingViewHolderNew extends RecyclerView.ViewHolder {
    public ImageView mConsultImageView;
    public TextView mViewTypingInProgress;
    private ChatStyle style;

    public ConsultIsTypingViewHolderNew(ViewGroup parent) {
        super((LayoutInflater.from(parent.getContext())).inflate(R.layout.item_consult_typing_new, parent, false));
        mConsultImageView = (ImageView) itemView.findViewById(R.id.image);
        mViewTypingInProgress = (TextView) itemView.findViewById(R.id.typing_in_progress);
        if (style == null) style = PrefUtils.getIncomingStyle(itemView.getContext());
        if (null != style) {
            if (style.welcomeScreenTextColorResId != ChatStyle.INVALID){
                mViewTypingInProgress.setTextColor(ContextCompat.getColor(itemView.getContext(), style.welcomeScreenTextColorResId));
            }
        }
    }

    public void onBind(View.OnClickListener consultClickListener) {
        mConsultImageView.setOnClickListener(consultClickListener);
    }
}
