package im.threads.adapters;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import im.threads.holders.GalleryItemHolder;
import im.threads.model.MediaPhoto;

/**
 * Created by yuri on 07.07.2016.
 */
public class GalleryAdaper extends RecyclerView.Adapter<GalleryItemHolder> {
    private List<MediaPhoto> list;
    private List<MediaPhoto> chosenList = new ArrayList<>();
    OnGalleryItemClick mOnGalleryItemClick;

    public GalleryAdaper(List<MediaPhoto> list, OnGalleryItemClick onGalleryItemClick) {
        this.list = list;
        mOnGalleryItemClick = onGalleryItemClick;
    }

    @Override
    public GalleryItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GalleryItemHolder(parent);
    }

    @Override
    public void onBindViewHolder(final GalleryItemHolder holder, int position) {
        holder.onBind(list.get(position).getImagePath(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
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
