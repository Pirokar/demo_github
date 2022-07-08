package im.threads.internal.holders;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.image_loading.CoilImageLoader;
import im.threads.internal.image_loading.ImageLoader;
import im.threads.internal.model.FileDescription;
import im.threads.internal.utils.ColorsHelper;
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
    private ImageLoader imageLoader = new CoilImageLoader();

    public FileAndMediaViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_and_media, parent, false));
        mImageButton = itemView.findViewById(R.id.file_button);
        mDownloadButton = itemView.findViewById(R.id.button_download_file);
        fileHeaderTextView = itemView.findViewById(R.id.file_title);
        fileSizeTextView = itemView.findViewById(R.id.file_size);
        timeStampTextView = itemView.findViewById(R.id.timestamp);
        ChatStyle style = Config.instance.getChatStyle();
        setUpTintedDrawable(style);
        fileSizeTextView.setTextColor(getColorInt(style.mediaAndFilesTextColor));
        fileHeaderTextView.setTextColor(getColorInt(style.mediaAndFilesTextColor));
        timeStampTextView.setTextColor(getColorInt(style.mediaAndFilesTextColor));
    }

    private void setUpTintedDrawable(ChatStyle style) {
        tintedDrawable = AppCompatResources.getDrawable(itemView.getContext(),
                style.mediaAndFilesFileIconResId);
        int tintResId = style.chatBodyIconsTint == 0 ? style.mediaAndFilesFileIconTintResId
                : style.chatBodyIconsTint;
        if (tintedDrawable != null) {
            ColorsHelper.setDrawableColor(itemView.getContext(), tintedDrawable, tintResId);
        }
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
            String downloadPath = "";
            if (fileDescription.getFileUri() != null) {
                downloadPath = fileDescription.getFileUri().toString();
            } else if (fileDescription.getDownloadPath() != null) {
                downloadPath = fileDescription.getDownloadPath();
            }
            imageLoader.loadImage(
                    mImageButton,
                    downloadPath,
                    List.of(ImageView.ScaleType.FIT_XY),
                    null
            );
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
