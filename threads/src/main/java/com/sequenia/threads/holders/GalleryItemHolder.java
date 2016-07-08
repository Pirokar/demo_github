package com.sequenia.threads.holders;

import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sequenia.threads.R;
import com.sequenia.threads.picasso_url_connection_only.Picasso;

import java.io.File;

/**
 * Created by yuri on 07.07.2016.
 */
public class GalleryItemHolder extends RecyclerView.ViewHolder {
    private ImageView mImageView;
    private AppCompatCheckBox mCheckBox;

    public GalleryItemHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_image, parent, false));
        mImageView = (ImageView) itemView.findViewById(R.id.image);
        mCheckBox = (AppCompatCheckBox) itemView.findViewById(R.id.checkbox);

    }

    public void onBind(String imagePath, View.OnClickListener listener, boolean isChecked) {
        Picasso
                .with(itemView.getContext())
                .load(new File(imagePath))
                .fit()
                .centerCrop()
                .into(mImageView);
        mCheckBox.setChecked(isChecked);
        mCheckBox.setOnClickListener(listener);
        mImageView.setOnClickListener(listener);
    }
}
