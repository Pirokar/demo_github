package im.threads.internal.adapters;

import android.net.Uri;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import im.threads.internal.holders.BottomGalleryImageHolder;
import im.threads.internal.model.BottomGalleryItem;

public final class BottomGalleryAdapter extends RecyclerView.Adapter<BottomGalleryImageHolder> {
    private List<BottomGalleryItem> list;
    private List<Uri> mChosenItems = new ArrayList<>();
    private OnChooseItemsListener mOnChooseItemsListener;

    public BottomGalleryAdapter(List<BottomGalleryItem> list, OnChooseItemsListener listener) {
        this.list = list;
        this.mOnChooseItemsListener = listener;
    }

    @NonNull
    @Override
    public BottomGalleryImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BottomGalleryImageHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull final BottomGalleryImageHolder holder, int position) {
        final BottomGalleryItem item = list.get(position);
        holder.onBind(list.get(position), v -> {
            item.setChosen(!item.isChosen());
            notifyItemChanged(holder.getAdapterPosition());
            mChosenItems.clear();
            for (BottomGalleryItem item1 : list) {
                if (item1.isChosen()) {
                    mChosenItems.add(item1.getImagePath());
                }
            }
            if (mOnChooseItemsListener != null) {
                mOnChooseItemsListener.onChosenItems(mChosenItems);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnChooseItemsListener {
        void onChosenItems(List<Uri> items);
    }
}