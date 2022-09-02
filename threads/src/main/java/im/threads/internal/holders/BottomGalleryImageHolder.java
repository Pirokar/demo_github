package im.threads.internal.holders;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import im.threads.R;
import im.threads.business.imageLoading.ImageLoader;
import im.threads.ui.models.BottomGalleryItem;
import im.threads.ui.utils.ColorsHelper;

public final class BottomGalleryImageHolder extends BaseHolder {
    private final ImageView image;
    private final ImageView chosenMark;

    public BottomGalleryImageHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gallery_bottom, parent, false), null);
        image = itemView.findViewById(R.id.image);
        chosenMark = itemView.findViewById(R.id.mark);
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
                .disableEdnaSsl()
                .scales(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_CROP)
                .into(image);
    }

    private void setChosenMarkBackgroundDrawable(@NonNull BottomGalleryItem item) {
        Drawable drawable;
        if (item.isChosen()) {
            drawable = AppCompatResources.getDrawable(itemView.getContext(),
                    getStyle().attachmentDoneIconResId);
            if (drawable != null) {
                int attachmentBottomSheetButtonTintResId = getStyle().chatBodyIconsTint == 0
                        ? getStyle().attachmentBottomSheetButtonTintResId
                        : getStyle().chatBodyIconsTint;
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
