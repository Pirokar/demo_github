package im.threads.holders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import im.threads.R;
import im.threads.model.ChatStyle;

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
        if (style == null) style = ChatStyle.getInstance();
        mViewTypingInProgress.setTextColor(ContextCompat.getColor(itemView.getContext(), style.chatSystemMessageTextColor));
        mConsultAvatar.getLayoutParams().height = (int) itemView.getContext().getResources().getDimension(style.operatorSystemAvatarSize);
        mConsultAvatar.getLayoutParams().width = (int) itemView.getContext().getResources().getDimension(style.operatorSystemAvatarSize);
    }

    public void onBind(View.OnClickListener consultClickListener) {
        mConsultAvatar.setOnClickListener(consultClickListener);
    }
}
