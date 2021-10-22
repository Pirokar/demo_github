package im.threads.internal.holders;

import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;

public final class SearchingConsultViewHolder extends RecyclerView.ViewHolder {

    public SearchingConsultViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_searching_consult, parent, false));
        ProgressBar progressBar = itemView.findViewById(R.id.progress);
        ChatStyle style = Config.instance.getChatStyle();
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(itemView.getContext(), style.chatToolbarColorResId), PorterDuff.Mode.SRC_ATOP);
        itemView.setVisibility(style.showConsultSearching ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(style.showConsultSearching ? View.VISIBLE : View.GONE);
    }
}
