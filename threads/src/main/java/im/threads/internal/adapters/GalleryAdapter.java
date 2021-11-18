package im.threads.internal.adapters;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import im.threads.R;
import im.threads.internal.Config;
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
                        Context context = Config.instance.context;
                        if (context != null) {
                            if (chosenList.size() >= context.getResources().getInteger(Config.instance.getChatStyle().maxGalleryImagesCount) ||
                                    chosenList.size() >= context.getResources().getInteger(Config.instance.getChatStyle().maxGalleryImagesCountFixedBySystem)
                            ) {
                                Toast.makeText(context, context.getString(R.string.threads_achieve_images_count_limit_mesage), Toast.LENGTH_SHORT)
                                        .show();
                            } else {
                                photo.setChecked(true);
                            }
                        }
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
