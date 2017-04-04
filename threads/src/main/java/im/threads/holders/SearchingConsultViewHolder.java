package im.threads.holders;

import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import im.threads.R;
import im.threads.model.ChatStyle;
import im.threads.utils.PrefUtils;

/**
 *
 */
public class SearchingConsultViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "SearchingViewHolder ";


    public SearchingConsultViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_searching_consult, parent, false));
        ProgressBar progressBar = (ProgressBar) itemView.findViewById(R.id.progress);

        ChatStyle style = PrefUtils.getIncomingStyle(itemView.getContext());
        if (style != null && style.chatToolbarColorResId != ChatStyle.INVALID) {
            progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(itemView.getContext(), style.chatToolbarColorResId), PorterDuff.Mode.SRC_ATOP);
        }

    }
}
