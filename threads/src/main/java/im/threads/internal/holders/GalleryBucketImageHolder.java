package im.threads.internal.holders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import im.threads.R;
import im.threads.internal.picasso_url_connection_only.Picasso;

import java.io.File;

public class GalleryBucketImageHolder extends RecyclerView.ViewHolder {
    private ImageView mImageView;
    private TextView mNameTextView;
    private TextView mSizeTextView;

    public GalleryBucketImageHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_bucket, parent, false));
        mImageView = itemView.findViewById(R.id.image);
        mNameTextView = itemView.findViewById(R.id.bucket_name);
        mSizeTextView = itemView.findViewById(R.id.photos_count);
    }

    public void onBind(String title, String count, String imagePath, View.OnClickListener itemClickListener) {
        ViewGroup vg = (ViewGroup) itemView;
        for (int i = 0; i < vg.getChildCount(); i++) {
            if (null != itemClickListener)
                vg.getChildAt(i).setOnClickListener(itemClickListener);
        }
        Picasso
                .with(itemView.getContext())
                .load(new File(imagePath))
                .fit()
                .centerCrop()
                .into(mImageView);
        mNameTextView.setText(title);
        mSizeTextView.setText(count);
    }
}
