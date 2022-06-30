package im.threads.internal.holders;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import im.threads.R;
import im.threads.internal.image_loading.CoilImageLoader;
import im.threads.internal.image_loading.ImageLoader;
import im.threads.internal.image_loading.ImageScale;

public final class GalleryBucketImageHolder extends RecyclerView.ViewHolder {
    private ImageView mImageView;
    private TextView mNameTextView;
    private TextView mSizeTextView;

    private ImageLoader imageLoader = new CoilImageLoader();

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

        imageLoader.loadImage(
                mImageView,
                imagePath.toString(),
                ImageScale.FIT,
                null
        );

        mNameTextView.setText(title);
        mSizeTextView.setText(count);
    }
}
