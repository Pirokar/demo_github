package im.threads.internal.holders;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.model.FileDescription;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.views.CircularProgressButton;

public final class FileAndMediaViewHolder extends BaseHolder {
    private ImageButton mImageButton;
    private CircularProgressButton mDownloadButton;
    private TextView fileHeaderTextView;
    private TextView fileSizeTextView;
    private TextView timeStampTextView;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private Drawable tintedDrawable;

    public FileAndMediaViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_and_media, parent, false));
        mImageButton = itemView.findViewById(R.id.file_button);
        mDownloadButton = itemView.findViewById(R.id.button_download_file);
        fileHeaderTextView = itemView.findViewById(R.id.file_title);
        fileSizeTextView = itemView.findViewById(R.id.file_size);
        timeStampTextView = itemView.findViewById(R.id.timestamp);
        tintedDrawable = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_insert_file_blue_36dp);
        ChatStyle style = Config.instance.getChatStyle();
        tintedDrawable.setColorFilter(ContextCompat.getColor(itemView.getContext(), style.chatBodyIconsTint), PorterDuff.Mode.SRC_ATOP);
        fileSizeTextView.setTextColor(getColorInt(style.mediaAndFilesTextColor));
        fileHeaderTextView.setTextColor(getColorInt(style.mediaAndFilesTextColor));
        timeStampTextView.setTextColor(getColorInt(style.mediaAndFilesTextColor));
    }

    public void onBind(FileDescription fileDescription,
                       View.OnClickListener fileClickListener,
                       View.OnClickListener fileDownloadListener
    ) {
        if (fileDescription.getFileUri() == null) {
            mDownloadButton.setVisibility(View.VISIBLE);
            mImageButton.setVisibility(View.GONE);
            mDownloadButton.setOnClickListener(fileDownloadListener);
        } else {
            mDownloadButton.setVisibility(View.GONE);
            mImageButton.setVisibility(View.VISIBLE);
        }
        mDownloadButton.setProgress(fileDescription.getFileUri() != null ? 100 : fileDescription.getDownloadProgress());
        if (FileUtils.isImage(fileDescription)) {
            if (fileDescription.getFileUri() != null) {
                Picasso.get()
                        .load(fileDescription.getFileUri())
                        .fit()
                        .centerInside()
                        .into(mImageButton);
            } else if (fileDescription.getDownloadPath() != null) {
                Picasso.get()
                        .load(fileDescription.getDownloadPath())
                        .fit()
                        .centerInside()
                        .into(mImageButton);
            }
        } else {
            mImageButton.setImageDrawable(tintedDrawable);
        }
        fileHeaderTextView.setText(FileUtils.getFileName(fileDescription));
        long fileSize = fileDescription.getSize();
        fileSizeTextView.setText(android.text.format.Formatter.formatFileSize(itemView.getContext(), fileSize));
        fileSizeTextView.setVisibility(fileSize > 0 ? View.VISIBLE : View.GONE);
        timeStampTextView.setText(sdf.format(new Date(fileDescription.getTimeStamp())));
        mImageButton.setOnClickListener(fileClickListener);
        ViewGroup vg = (ViewGroup) itemView;
        for (int i = 0; i < vg.getChildCount(); i++) {
            vg.getChildAt(i).setOnClickListener(fileClickListener);
        }
    }
}
