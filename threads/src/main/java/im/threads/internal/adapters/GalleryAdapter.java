package im.threads.internal.adapters;

import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import im.threads.internal.holders.GalleryItemHolder;
import im.threads.internal.model.MediaPhoto;

public final class GalleryAdapter extends RecyclerView.Adapter<GalleryItemHolder> {
    private final List<MediaPhoto> list;
    private final List<MediaPhoto> chosenList = new ArrayList<>();
    private final OnGalleryItemClick onGalleryItemClick;

    public GalleryAdapter(List<MediaPhoto> list, OnGalleryItemClick onGalleryItemClick) {
        this.list = list;
        this.onGalleryItemClick = onGalleryItemClick;
    }

    @NonNull
    @Override
    public GalleryItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GalleryItemHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull final GalleryItemHolder holder, int position) {
        holder.onBind(
                list.get(position).getImageUri(),
                v -> {
                    MediaPhoto photo = list.get(holder.getAdapterPosition());
                    if (photo.isChecked()) {
                        photo.setChecked(false);
                    } else {
                        photo.setChecked(true);
                    }
                    if (null != onGalleryItemClick) {
                        chosenList.clear();
                        for (MediaPhoto mp : list) {
                            if (mp.isChecked()) {
                                chosenList.add(mp);
                            }
                        }
                        onGalleryItemClick.onGalleryItemsChosen(chosenList);
                    }
                    notifyItemChanged(holder.getAdapterPosition());
                },
                list.get(position).isChecked()
        );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnGalleryItemClick {
        void onGalleryItemsChosen(List<MediaPhoto> chosenItems);
    }
}
