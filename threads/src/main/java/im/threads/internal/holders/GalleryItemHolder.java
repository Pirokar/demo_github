package im.threads.internal.holders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;

import im.threads.R;

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
            Picasso.get()
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
