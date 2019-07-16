package im.threads.holders;

import android.graphics.drawable.Drawable;
import android.support.v7.content.res.AppCompatResources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;

import im.threads.R;
import im.threads.model.BottomGalleryItem;
import im.threads.model.ChatStyle;
import im.threads.picasso_url_connection_only.Picasso;

/**
 * Created by yuri on 30.06.2016.
 */
public class BottomGalleryImageHolder extends BaseHolder {
    private ImageView image;
    private ImageView chosenMark;
    private ChatStyle style;

    public BottomGalleryImageHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_bottom, parent, false));
        image = (ImageView) itemView.findViewById(R.id.image);
        chosenMark = (ImageView) itemView.findViewById(R.id.mark);
        if (style == null) style = ChatStyle.getInstance();
    }

    public void onBind(BottomGalleryItem item, View.OnClickListener listener) {
        ViewGroup vg = (ViewGroup) itemView;
        for (int i = 0; i < vg.getChildCount(); i++) {
            vg.getChildAt(i).setOnClickListener(listener);
        }
        Drawable d;
        if (item.isChosen()) {
            d = (AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_circle_done_blue_36dp));
            setTintToViews(new Drawable[]{d}, style.chatBodyIconsTint);
        } else {
            d = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_panorama_fish_eye_white_36dp);
        }
        chosenMark.setBackground(d);
        Picasso
                .with(itemView.getContext())
                .load(new File(item.getImagePath()))
                .fit()
                .centerCrop()
                .into(image);
    }
}
