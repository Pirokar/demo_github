package com.sequenia.threads.holders;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sequenia.threads.R;
import com.sequenia.threads.model.BottomGalleryItem;
import com.sequenia.threads.model.ChatStyle;
import com.sequenia.threads.picasso_url_connection_only.Picasso;
import com.sequenia.threads.utils.PrefUtils;

import static com.sequenia.threads.model.ChatStyle.INVALID;

/**
 * Created by yuri on 30.06.2016.
 */
public class BottomGalleryImageHolder extends BaseHolder {
    private ImageView image;
    private ImageView chosenMark;
    private static ChatStyle style;

    public BottomGalleryImageHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_bottom, parent, false));
        image = (ImageView) itemView.findViewById(R.id.image);
        chosenMark = (ImageView) itemView.findViewById(R.id.mark);
        if (style == null) style = PrefUtils.getIncomingStyle(itemView.getContext());
    }

    public void onBind(BottomGalleryItem item, View.OnClickListener listener) {
        ViewGroup vg = (ViewGroup) itemView;
        for (int i = 0; i < vg.getChildCount(); i++) {
            vg.getChildAt(i).setOnClickListener(listener);
        }
        Drawable d;
        if (item.isChosen()) {
            d = (itemView.getResources().getDrawable(R.drawable.ic_circle_done_blue_36dp));
            if (style != null && style.chatBodyIconsTint != INVALID)
                setTintToViews(new Drawable[]{d}, style.chatBodyIconsTint);
        } else {
            d = itemView.getResources().getDrawable(R.drawable.ic_panorama_fish_eye_white_36dp);
        }
        chosenMark.setBackground(d);
        Picasso
                .with(itemView.getContext())
                .load(item.getImagePath())
                .fit()
                .centerCrop()
                .into(image);
    }
}
