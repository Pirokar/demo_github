package im.threads.holders;

import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import im.threads.R;
import im.threads.model.ChatStyle;

/**
 *
 */
public class SearchingConsultViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "SearchingViewHolder ";

    private ChatStyle style;


    public SearchingConsultViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_searching_consult, parent, false));
        ProgressBar progressBar = (ProgressBar) itemView.findViewById(R.id.progress);

        if (style == null) style = ChatStyle.getInstance();
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(itemView.getContext(), style.chatToolbarColorResId), PorterDuff.Mode.SRC_ATOP);
        if (style.showConsultSearching) {
            itemView.setVisibility(View.VISIBLE);
        } else {
            itemView.setVisibility(View.GONE);
        }
    }
}
