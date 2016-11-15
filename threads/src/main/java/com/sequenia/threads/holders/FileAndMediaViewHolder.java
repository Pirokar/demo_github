package com.sequenia.threads.holders;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sequenia.threads.R;
import com.sequenia.threads.model.ChatStyle;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.picasso_url_connection_only.Picasso;
import com.sequenia.threads.utils.FileUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.sequenia.threads.model.ChatStyle.INVALID;

/**
 * Created by yuri on 01.07.2016.
 */
public class FileAndMediaViewHolder extends BaseHolder {
    private static final String TAG = "FileAndMediaViewHolder ";
    private ImageButton mImageButton;
    private TextView fileHeaderTextView;
    private TextView fileSizeTextView;
    private TextView timeStampTextView;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    private static ChatStyle style;
    private Drawable tintedDrawable;

    public FileAndMediaViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_and_media, parent, false));
        mImageButton = (ImageButton) itemView.findViewById(R.id.file_button);
        fileHeaderTextView = (TextView) itemView.findViewById(R.id.file_title);
        fileSizeTextView = (TextView) itemView.findViewById(R.id.file_size);
        timeStampTextView = (TextView) itemView.findViewById(R.id.timestamp);
        tintedDrawable = itemView.getResources().getDrawable(R.drawable.ic_insert_file_blue_36dp);
        if (style != null) {
            if (style.incomingMessageTextColor != INVALID){
                setTextColorToViews(new TextView[]{fileHeaderTextView,fileSizeTextView,timeStampTextView},style.incomingMessageTextColor);
            }

            if (style.chatBodyIconsTint != INVALID) {
                tintedDrawable.setColorFilter(ContextCompat.getColor(itemView.getContext(), style.chatBodyIconsTint), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    public void onBind(
            FileDescription fileDescription
            , View.OnClickListener fileClickListener) {
        int extension = FileUtils.getExtensionFromPath(fileDescription.getFilePath()) == FileUtils.UNKNOWN ?
                FileUtils.getExtensionFromPath(fileDescription.getIncomingName())
                : FileUtils.getExtensionFromPath(fileDescription.getFilePath());
        Picasso p = Picasso.with(itemView.getContext());
        if (extension == FileUtils.PDF || extension == FileUtils.OTHER_DOC_FORMATS) {
            mImageButton.setImageDrawable(tintedDrawable);
        } else if (extension == FileUtils.JPEG || extension == FileUtils.PNG) {
            if (fileDescription.getFilePath() != null) {
                p
                        .load(fileDescription.getFilePath())
                        .fit()
                        .centerInside()
                        .into(mImageButton);
            } else if (fileDescription.getDownloadPath() != null) {
                p
                        .load(fileDescription.getDownloadPath())
                        .fit()
                        .centerInside()
                        .into(mImageButton);
            }

        } else {
            mImageButton.setImageDrawable(tintedDrawable);
        }
        String header = "";
        if (fileDescription.getFilePath() != null) {
            header = FileUtils.getLastPathSegment(fileDescription.getFilePath());
        } else if (fileDescription.getIncomingName() != null) {
            header = fileDescription.getIncomingName();
        }
        fileHeaderTextView.setText(header);
        fileSizeTextView.setText(android.text.format.Formatter.formatFileSize(itemView.getContext(), fileDescription.getSize()));
        timeStampTextView.setText(sdf.format(new Date(fileDescription.getTimeStamp())));
        mImageButton.setOnClickListener(fileClickListener);
        ViewGroup vg = (ViewGroup) itemView;
        for (int i = 0; i < vg.getChildCount(); i++) {
            vg.getChildAt(i).setOnClickListener(fileClickListener);
        }
    }
}
