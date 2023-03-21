package im.threads.ui.holders;

import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import im.threads.ui.ChatStyle;
import im.threads.R;
import im.threads.ui.config.Config;

public final class SearchingConsultViewHolder extends RecyclerView.ViewHolder {

    public SearchingConsultViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.ecc_item_searching_consult, parent, false));
        ProgressBar progressBar = itemView.findViewById(R.id.progress);
        ChatStyle style = Config.getInstance().getChatStyle();
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(itemView.getContext(), style.consultSearchingProgressColor), PorterDuff.Mode.SRC_ATOP);
        itemView.setVisibility(style.showConsultSearching ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(style.showConsultSearching ? View.VISIBLE : View.GONE);
    }
}
