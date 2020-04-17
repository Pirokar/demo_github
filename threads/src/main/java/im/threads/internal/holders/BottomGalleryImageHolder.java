package im.threads.internal.holders;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.content.res.AppCompatResources;

import com.squareup.picasso.Picasso;

import java.io.File;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.model.BottomGalleryItem;

public final class BottomGalleryImageHolder extends BaseHolder {
    private ImageView image;
    private ImageView chosenMark;
    private ChatStyle style;

    public BottomGalleryImageHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_bottom, parent, false));
        image = itemView.findViewById(R.id.image);
        chosenMark = itemView.findViewById(R.id.mark);
        if (style == null) style = Config.instance.getChatStyle();
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
        Picasso.get()
                .load(new File(item.getImagePath()))
                .fit()
                .centerCrop()
                .into(image);
    }
}
