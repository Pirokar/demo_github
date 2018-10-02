package im.threads.holders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;
import im.threads.R;
import im.threads.picasso_url_connection_only.Picasso;

/**
 * Created by yuri on 07.07.2016.
 */
public class GalleryItemHolder extends RecyclerView.ViewHolder {
    private ImageView mImageView;
    private AppCompatCheckBox mCheckBox;

    public GalleryItemHolder(final ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_image, parent, false));
        mImageView = (ImageView) itemView.findViewById(R.id.image);
        mCheckBox = (AppCompatCheckBox) itemView.findViewById(R.id.checkbox);

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
