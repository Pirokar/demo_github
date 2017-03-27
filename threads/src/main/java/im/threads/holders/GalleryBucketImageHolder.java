package im.threads.holders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import im.threads.R;
import im.threads.picasso_url_connection_only.Picasso;

import java.io.File;

/**
 * Created by yuri on 06.07.2016.
 */
public class GalleryBucketImageHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "GalleryBucketImageHolder ";
    private ImageView mImageView;
    private TextView mNameTextView;
    private TextView mSizeTextView;

    public GalleryBucketImageHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_bucket, parent, false));
        mImageView = (ImageView) itemView.findViewById(R.id.image);
        mNameTextView = (TextView) itemView.findViewById(R.id.bucket_name);
        mSizeTextView = (TextView) itemView.findViewById(R.id.photos_count);
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
