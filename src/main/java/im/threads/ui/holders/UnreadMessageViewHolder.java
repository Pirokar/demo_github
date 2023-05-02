package im.threads.ui.holders;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import im.threads.ui.ChatStyle;
import im.threads.R;
import im.threads.business.models.UnreadMessages;
import im.threads.ui.config.Config;

public final class UnreadMessageViewHolder extends RecyclerView.ViewHolder {
    private TextView mTextView;

    public UnreadMessageViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.ecc_item_unread_counter, parent, false));
        mTextView = itemView.findViewById(R.id.text);
        ChatStyle style = Config.getInstance().getChatStyle();
        mTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), style.systemMessageTextColorResId));
    }

    public void onBind(UnreadMessages unreadMessages) {
        mTextView.setText(unreadMessages.getMessage(mTextView.getContext()));
    }
}