package im.threads.ui.holders;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import im.threads.R;
import im.threads.business.imageLoading.ImageLoader;

public final class GalleryBucketImageHolder extends RecyclerView.ViewHolder {
    private ImageView mImageView;
    private TextView mNameTextView;
    private TextView mSizeTextView;

    public GalleryBucketImageHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_bucket, parent, false));
        mImageView = itemView.findViewById(R.id.image);
        mNameTextView = itemView.findViewById(R.id.bucket_name);
        mSizeTextView = itemView.findViewById(R.id.photos_count);
    }

    public void onBind(String title, String count, Uri imagePath, View.OnClickListener itemClickListener) {
        ViewGroup vg = (ViewGroup) itemView;
        for (int i = 0; i < vg.getChildCount(); i++) {
            if (null != itemClickListener)
                vg.getChildAt(i).setOnClickListener(itemClickListener);
        }

        ImageLoader
                .get()
                .load(imagePath.toString())
                .disableEdnaSsl()
                .autoRotateWithExif(true)
                .scales(ImageView.ScaleType.FIT_CENTER, ImageView.ScaleType.CENTER_CROP)
                .into(mImageView);

        mNameTextView.setText(title);
        mSizeTextView.setText(count);
    }
}
