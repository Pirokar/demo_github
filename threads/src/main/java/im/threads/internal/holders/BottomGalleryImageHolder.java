package im.threads.internal.holders;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.image_loading.ImageLoader;
import im.threads.internal.model.BottomGalleryItem;
import im.threads.internal.utils.ColorsHelper;

public final class BottomGalleryImageHolder extends BaseHolder {
    private final ImageView image;
    private final ImageView chosenMark;
    private final ChatStyle style;

    public BottomGalleryImageHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gallery_bottom, parent, false));
        image = itemView.findViewById(R.id.image);
        chosenMark = itemView.findViewById(R.id.mark);
        style = Config.instance.getChatStyle();
    }

    public void onBind(@NonNull BottomGalleryItem item, @NonNull View.OnClickListener listener) {
        ViewGroup vg = (ViewGroup) itemView;
        for (int i = 0; i < vg.getChildCount(); i++) {
            vg.getChildAt(i).setOnClickListener(listener);
        }
        setChosenMarkBackgroundDrawable(item);
        ImageLoader
                .get()
                .load(item.getImagePath().toString())
                .scales(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_CROP)
                .into(image);
    }

    private void setChosenMarkBackgroundDrawable(@NonNull BottomGalleryItem item) {
        Drawable drawable;
        if (item.isChosen()) {
            drawable = AppCompatResources.getDrawable(itemView.getContext(),
                    style.attachmentDoneIconResId);
            if (drawable != null) {
                int attachmentBottomSheetButtonTintResId = style.chatBodyIconsTint == 0
                        ? style.attachmentBottomSheetButtonTintResId
                        : style.chatBodyIconsTint;
                ColorsHelper.setDrawableColor(itemView.getContext(), drawable.mutate(),
                        attachmentBottomSheetButtonTintResId);
            }
        } else {
            drawable = AppCompatResources.getDrawable(itemView.getContext(),
                    R.drawable.ic_panorama_fish_eye_white_36dp);
        }
        chosenMark.setBackground(drawable);
    }
}
