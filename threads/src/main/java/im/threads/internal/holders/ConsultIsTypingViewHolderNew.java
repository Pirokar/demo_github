package im.threads.internal.holders;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import im.threads.R;
import im.threads.internal.Config;
import im.threads.ChatStyle;

/**
 * Created by yuri on 09.06.2016.
 * layout/item_consult_typing.xml
 */
public class ConsultIsTypingViewHolderNew extends RecyclerView.ViewHolder {
    public ImageView mConsultAvatar;
    public TextView mViewTypingInProgress;
    private ChatStyle style;

    public ConsultIsTypingViewHolderNew(ViewGroup parent) {
        super((LayoutInflater.from(parent.getContext())).inflate(R.layout.item_consult_typing, parent, false));
        mConsultAvatar = (ImageView) itemView.findViewById(R.id.image);
        mViewTypingInProgress = (TextView) itemView.findViewById(R.id.typing_in_progress);
        if (style == null) style = Config.instance.getChatStyle();
        mViewTypingInProgress.setTextColor(ContextCompat.getColor(itemView.getContext(), style.chatSystemMessageTextColor));
        mConsultAvatar.getLayoutParams().height = (int) itemView.getContext().getResources().getDimension(style.operatorSystemAvatarSize);
        mConsultAvatar.getLayoutParams().width = (int) itemView.getContext().getResources().getDimension(style.operatorSystemAvatarSize);
    }

    public void onBind(View.OnClickListener consultClickListener) {
        mConsultAvatar.setOnClickListener(consultClickListener);
    }
}
