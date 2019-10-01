package im.threads.internal.holders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;

/**
 * layout/item_consult_typing.xml
 */
public final class ConsultIsTypingViewHolderNew extends RecyclerView.ViewHolder {
    public ImageView mConsultAvatar;
    private TextView mViewTypingInProgress;
    private ChatStyle style;

    public ConsultIsTypingViewHolderNew(ViewGroup parent) {
        super((LayoutInflater.from(parent.getContext())).inflate(R.layout.item_consult_typing, parent, false));
        mConsultAvatar = itemView.findViewById(R.id.image);
        mViewTypingInProgress = itemView.findViewById(R.id.typing_in_progress);
        if (style == null) style = Config.instance.getChatStyle();
        mViewTypingInProgress.setTextColor(ContextCompat.getColor(itemView.getContext(), style.chatSystemMessageTextColor));
        mConsultAvatar.getLayoutParams().height = (int) itemView.getContext().getResources().getDimension(style.operatorSystemAvatarSize);
        mConsultAvatar.getLayoutParams().width = (int) itemView.getContext().getResources().getDimension(style.operatorSystemAvatarSize);
    }

    public void onBind(View.OnClickListener consultClickListener) {
        mConsultAvatar.setOnClickListener(consultClickListener);
    }
}
