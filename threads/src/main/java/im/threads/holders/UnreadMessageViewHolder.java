package im.threads.holders;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import im.threads.R;
import im.threads.model.ChatStyle;
import im.threads.model.UnreadMessages;
import im.threads.utils.PrefUtils;

/**
 * Created by yuri on 24.08.2016.
 */
public class UnreadMessageViewHolder extends RecyclerView.ViewHolder {
    private TextView mTextView;
    private static ChatStyle style;

    public UnreadMessageViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unread_counter, parent, false));
        mTextView = (TextView) itemView.findViewById(R.id.text);
        if (style == null) style = PrefUtils.getIncomingStyle(itemView.getContext());
        if (null != style && style.connectionMessageTextColor != ChatStyle.INVALID) {
            mTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), style.connectionMessageTextColor));
        }
    }

    public void onBind(UnreadMessages unreadMessages) {
        mTextView.setText(unreadMessages.getMessage());
    }

}
