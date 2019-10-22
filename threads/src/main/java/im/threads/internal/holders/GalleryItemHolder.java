package im.threads.internal.holders;

import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;

import im.threads.R;
import im.threads.internal.picasso_url_connection_only.Picasso;

public final class GalleryItemHolder extends RecyclerView.ViewHolder {
    private ImageView mImageView;
    private AppCompatCheckBox mCheckBox;

    public GalleryItemHolder(final ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_image, parent, false));

        mImageView = itemView.findViewById(R.id.image);
        mCheckBox = itemView.findViewById(R.id.checkbox);
        mCheckBox.setButtonDrawable(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.bk_checkbox_blue));
    }

    public void onBind(final String imagePath, final View.OnClickListener listener, final boolean isChecked) {
        if (mImageView != null) {
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
}
