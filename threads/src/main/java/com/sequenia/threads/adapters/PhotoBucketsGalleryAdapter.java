package com.sequenia.threads.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.sequenia.threads.holders.GalleryBucketImageHolder;
import com.sequenia.threads.model.PhotoBucketItem;

import java.util.List;

/**
 * Created by yuri on 06.07.2016.
 */
public class PhotoBucketsGalleryAdapter extends RecyclerView.Adapter<GalleryBucketImageHolder> {
    private List<PhotoBucketItem> list;
    private OnItemClick mOnItemClick;

    public PhotoBucketsGalleryAdapter(List<PhotoBucketItem> list, OnItemClick onItemClick) {
        if (list == null) {
            throw new IllegalStateException("list must not be null");
        }
        this.list = list;
        mOnItemClick = onItemClick;
    }

    @Override
    public GalleryBucketImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GalleryBucketImageHolder(parent);
    }

    @Override
    public void onBindViewHolder(final GalleryBucketImageHolder holder, int position) {
        holder.onBind(list.get(position).getBucketName()
                , list.get(position).getBucketSize()
                , list.get(position).getImagePath()
                , new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mOnItemClick)
                            mOnItemClick.onPhotoBucketClick(list.get(holder.getAdapterPosition()));
                    }
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnItemClick {
        void onPhotoBucketClick(PhotoBucketItem item);
    }
}
