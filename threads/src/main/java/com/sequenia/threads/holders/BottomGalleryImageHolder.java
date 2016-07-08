package com.sequenia.threads.holders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sequenia.threads.R;
import com.sequenia.threads.model.BottomGalleryItem;
import com.sequenia.threads.picasso_url_connection_only.Picasso;

/**
 * Created by yuri on 30.06.2016.
 */
public class BottomGalleryImageHolder extends RecyclerView.ViewHolder {
    private ImageView image;
    private View chosenMark;

    public BottomGalleryImageHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_bottom, parent, false));
        image = (ImageView) itemView.findViewById(R.id.image);
        chosenMark = itemView.findViewById(R.id.mark);
    }

    public void onBind(BottomGalleryItem item, View.OnClickListener listener) {
        ViewGroup vg = (ViewGroup) itemView;
        for (int i = 0; i < vg.getChildCount(); i++) {
            vg.getChildAt(i).setOnClickListener(listener);
        }
        if (item.isChosen()) {
            chosenMark.setBackground(itemView.getResources().getDrawable(R.drawable.ic_circle_done_blue_36dp));
        }else {
            chosenMark.setBackground(itemView.getResources().getDrawable(R.drawable.ic_panorama_fish_eye_white_36dp));
        }
        Picasso
                .with(itemView.getContext())
                .load(item.getImagePath())
                .fit()
                .centerCrop()
                .into(image);
    }
}
