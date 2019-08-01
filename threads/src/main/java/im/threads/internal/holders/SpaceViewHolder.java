package im.threads.internal.holders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import im.threads.R;

/**
 * Created by yuri on 14.06.2016.
 * layout/item_free_space.xml
 * app prototype determines different item spacing ,that depends on items sequence,
 * so this view holder do that job;
 */
public class SpaceViewHolder extends RecyclerView.ViewHolder {
    private View root;

    public SpaceViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_free_space, parent, false));
        root = itemView.findViewById(R.id.root);
    }

    public void onBind(int height) {
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,height));
    }
}
