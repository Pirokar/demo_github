package com.sequenia.threads.holders;

import android.content.ContentResolver;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sequenia.threads.R;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.picasso_url_connection_only.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yuri on 01.07.2016.
 */
public class FileAndMediaViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "FileAndMediaViewHolder ";
    private ImageButton mImageButton;
    private TextView fileHeaderTextView;
    private TextView fileSizeTextView;
    private TextView timeStampTextView;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    public FileAndMediaViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_and_media, parent, false));
        mImageButton = (ImageButton) itemView.findViewById(R.id.file_button);
        fileHeaderTextView = (TextView) itemView.findViewById(R.id.file_title);
        fileSizeTextView = (TextView) itemView.findViewById(R.id.file_size);
        timeStampTextView = (TextView) itemView.findViewById(R.id.timestamp);
    }

    public void onBind(
            FileDescription fileDescription
            , View.OnClickListener fileClickListener) {

        if (fileDescription.getPath() == null) return;
        String extension = fileDescription.getPath().substring(fileDescription.getPath().lastIndexOf(".") + 1);
        Picasso p = Picasso.with(itemView.getContext());
        if (extension.equalsIgnoreCase("pdf")) {
            mImageButton.setImageResource(R.drawable.ic_insert_file_blue_36dp);
        } else if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("png")) {
            p
                    .load(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + itemView.getContext().getPackageName() + "/drawable/sample_card"))
                    .fit()
                    .centerInside()
                    .into(mImageButton);
        } else {
            mImageButton.setImageResource(R.drawable.ic_insert_file_blue_36dp);
        }
        fileHeaderTextView.setText(fileDescription.getPath());
        fileSizeTextView.setText("1,2 mb");
        timeStampTextView.setText(sdf.format(new Date(fileDescription.getTimeStamp())));
        mImageButton.setOnClickListener(fileClickListener);
    }
}
