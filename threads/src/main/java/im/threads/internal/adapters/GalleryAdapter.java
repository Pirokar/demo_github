package im.threads.internal.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import im.threads.internal.holders.GalleryItemHolder;
import im.threads.internal.model.MediaPhoto;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryItemHolder> {
    private List<MediaPhoto> list;
    private List<MediaPhoto> chosenList = new ArrayList<>();
    OnGalleryItemClick mOnGalleryItemClick;

    public GalleryAdapter(List<MediaPhoto> list, OnGalleryItemClick onGalleryItemClick) {
        this.list = list;
        mOnGalleryItemClick = onGalleryItemClick;
    }

    @Override
    public GalleryItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GalleryItemHolder(parent);
    }

    @Override
    public void onBindViewHolder(final GalleryItemHolder holder, int position) {
        holder.onBind(list.get(position).getImagePath(), v -> {
            MediaPhoto photo = list.get(holder.getAdapterPosition());
            if (photo.isChecked()) {
                photo.setChecked(false);
            } else {
                photo.setChecked(true);
            }
            if (null != mOnGalleryItemClick) {
                chosenList.clear();
                for (MediaPhoto mp : list
                ) {
                    if (mp.isChecked()) {
                        chosenList.add(mp);
                    }
                }
                mOnGalleryItemClick.onGalleryItemsChosen(chosenList);
            }
            notifyItemChanged(holder.getAdapterPosition());
        }, list.get(position).isChecked());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnGalleryItemClick {
        void onGalleryItemsChosen(List<MediaPhoto> chosenItems);
    }
}
