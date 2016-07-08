package com.sequenia.threads.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.sequenia.threads.holders.BottomGalleryImageHolder;
import com.sequenia.threads.model.BottomGalleryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuri on 30.06.2016.
 */
public class BottomGalleryAdapter extends RecyclerView.Adapter<BottomGalleryImageHolder> {
    private List<BottomGalleryItem> list;
    private List<String> mChosenItems = new ArrayList<>();
    private OnChooseItemsListener mOnChooseItemsListener;

    public BottomGalleryAdapter(List<BottomGalleryItem> list, OnChooseItemsListener listener) {
        this.list = list;
        this.mOnChooseItemsListener = listener;
    }

    @Override
    public BottomGalleryImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BottomGalleryImageHolder(parent);
    }

    @Override
    public void onBindViewHolder(final BottomGalleryImageHolder holder, int position) {
        final BottomGalleryItem item = list.get(position);
        holder.onBind(list.get(position), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.isChosen()) {
                    item.setChosen(false);
                } else if (!item.isChosen()) {//yes, i know;
                    item.setChosen(true);
                }
                notifyItemChanged(holder.getAdapterPosition());
                mChosenItems.clear();
                for (BottomGalleryItem item : list) {
                    if (item.isChosen()) {
                        mChosenItems.add(item.getImagePath());
                    }
                }
                if (null != mOnChooseItemsListener) {
                    mOnChooseItemsListener.onChosenItems(mChosenItems);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnChooseItemsListener {
        void onChosenItems(List<String> items);
    }
}
