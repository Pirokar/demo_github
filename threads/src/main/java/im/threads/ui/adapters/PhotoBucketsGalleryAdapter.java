package im.threads.ui.adapters;

import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import im.threads.internal.holders.GalleryBucketImageHolder;
import im.threads.internal.model.PhotoBucketItem;

public final class PhotoBucketsGalleryAdapter extends RecyclerView.Adapter<GalleryBucketImageHolder> {
    private List<PhotoBucketItem> list;
    private OnItemClick mOnItemClick;

    public PhotoBucketsGalleryAdapter(List<PhotoBucketItem> list, OnItemClick onItemClick) {
        if (list == null) {
            throw new IllegalStateException("list must not be null");
        }
        this.list = list;
        mOnItemClick = onItemClick;
    }

    @NonNull
    @Override
    public GalleryBucketImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GalleryBucketImageHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull final GalleryBucketImageHolder holder, int position) {
        holder.onBind(
                list.get(position).getBucketName(),
                list.get(position).getBucketSize(),
                list.get(position).getImagePath(),
                v -> {
                    if (null != mOnItemClick)
                        mOnItemClick.onPhotoBucketClick(list.get(holder.getAdapterPosition()));
                }
        );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnItemClick {
        void onPhotoBucketClick(PhotoBucketItem item);
    }
}
