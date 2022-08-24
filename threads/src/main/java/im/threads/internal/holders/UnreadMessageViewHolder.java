package im.threads.internal.holders;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.config.BaseConfig;
import im.threads.internal.model.UnreadMessages;
import im.threads.ui.Config;

public final class UnreadMessageViewHolder extends RecyclerView.ViewHolder {
    private TextView mTextView;

    public UnreadMessageViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unread_counter, parent, false));
        mTextView = itemView.findViewById(R.id.text);
        ChatStyle style = ((Config)BaseConfig.instance).getChatStyle();
        mTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), style.chatSystemMessageTextColor));
    }

    public void onBind(UnreadMessages unreadMessages) {
        mTextView.setText(unreadMessages.getMessage(mTextView.getContext()));
    }
}
