package im.threads.ui.holders;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;
import im.threads.R;

public final class EmptyViewHolder extends RecyclerView.ViewHolder {
    public EmptyViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.ecc_item_empty, parent, false));
    }
}
