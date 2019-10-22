package im.threads.internal.holders;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.model.UnreadMessages;

public final class UnreadMessageViewHolder extends RecyclerView.ViewHolder {
    private TextView mTextView;

    public UnreadMessageViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unread_counter, parent, false));
        mTextView = itemView.findViewById(R.id.text);
        ChatStyle style = Config.instance.getChatStyle();
        mTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), style.chatSystemMessageTextColor));
    }

    public void onBind(UnreadMessages unreadMessages) {
        mTextView.setText(unreadMessages.getMessage(mTextView.getContext()));
    }
}
