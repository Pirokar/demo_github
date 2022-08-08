package im.threads.internal.holders;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.imageLoading.ImageLoader;
import im.threads.internal.utils.ColorsHelper;

public final class GalleryItemHolder extends RecyclerView.ViewHolder {
    private final ImageView mImageView;
    private final AppCompatCheckBox mCheckBox;
    private final ChatStyle mStyle;

    public GalleryItemHolder(final ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_image, parent, false));

        mImageView = itemView.findViewById(R.id.image);
        mCheckBox = itemView.findViewById(R.id.checkbox);
        mStyle = Config.instance.getChatStyle();
    }

    public void onBind(final Uri imagePath, final View.OnClickListener listener, final boolean isChecked) {
        if (mImageView != null) {
            ImageLoader
                    .get()
                    .load(imagePath.toString())
                    .scales(ImageView.ScaleType.FIT_CENTER, ImageView.ScaleType.CENTER_CROP)
                    .into(mImageView);
            mCheckBox.setChecked(isChecked);
            setButtonDrawable(isChecked);
            mCheckBox.setOnClickListener(listener);
            mImageView.setOnClickListener(listener);
        }
    }

    private void setButtonDrawable(boolean isChecked) {
        Drawable drawable;
        if (isChecked) {
            drawable = AppCompatResources.getDrawable(itemView.getContext(),
                    mStyle.attachmentDoneIconResId);
            if (drawable != null) {
                int attachmentBottomSheetButtonTintResId = mStyle.chatBodyIconsTint == 0
                        ? mStyle.attachmentBottomSheetButtonTintResId
                        : mStyle.chatBodyIconsTint;
                ColorsHelper.setDrawableColor(itemView.getContext(), drawable.mutate(),
                        attachmentBottomSheetButtonTintResId);
            }
        } else {
            drawable = AppCompatResources.getDrawable(itemView.getContext(),
                    R.drawable.ic_panorama_fish_eye_white_36dp);
        }
        mCheckBox.setButtonDrawable(drawable);
    }
}
