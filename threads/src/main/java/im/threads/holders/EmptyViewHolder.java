package im.threads.holders;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;
import im.threads.R;

public class EmptyViewHolder extends RecyclerView.ViewHolder {
    public EmptyViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_empty, parent, false));
    }
}
