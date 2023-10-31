package im.threads.ui.holders;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

import im.threads.ui.ChatStyle;
import im.threads.R;
import im.threads.business.imageLoading.ImageLoader;
import im.threads.ui.utils.ColorsHelper;
import im.threads.ui.config.Config;

public final class GalleryItemHolder extends RecyclerView.ViewHolder {
    private final ImageView mImageView;
    private final AppCompatCheckBox mCheckBox;
    private final ChatStyle mStyle;

    public GalleryItemHolder(final ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.ecc_item_gallery_image, parent, false));

        mImageView = itemView.findViewById(R.id.image);
        mCheckBox = itemView.findViewById(R.id.checkbox);
        mStyle = Config.getInstance().getChatStyle();
    }

    public void onBind(final Uri imagePath, final View.OnClickListener listener, final boolean isChecked) {
        if (mImageView != null) {
            ImageLoader
                    .get()
                    .load(imagePath.toString())
                    .disableEdnaSsl()
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
                    R.drawable.ecc_ic_panorama_fish_eye_white_36dp);
        }
        mCheckBox.setButtonDrawable(drawable);
    }
}