package im.threads.ui.holders;

import android.content.Context;
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
import java.util.Locale;

import im.threads.ui.ChatStyle;
import im.threads.R;
import im.threads.business.imageLoading.ImageLoader;
import im.threads.business.models.FileDescription;
import im.threads.business.utils.FileUtils;
import im.threads.business.utils.FileUtilsKt;
import im.threads.ui.utils.ColorsHelper;
import im.threads.ui.views.CircularProgressButton;
import kotlin.Unit;

public final class FileAndMediaViewHolder extends BaseHolder {
    private ImageButton mImageButton;
    private CircularProgressButton mDownloadButton;
    private TextView fileHeaderTextView;
    private TextView fileSizeTextView;
    private TextView timeStampTextView;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private Drawable tintedDrawable;
    private Context context;

    public FileAndMediaViewHolder(ViewGroup parent) {
        super(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.ecc_item_file_and_media, parent, false),
                null,
                null
        );
        context = parent.getContext();

        mImageButton = itemView.findViewById(R.id.file_button);
        mDownloadButton = itemView.findViewById(R.id.button_download_file);
        fileHeaderTextView = itemView.findViewById(R.id.file_title);
        fileSizeTextView = itemView.findViewById(R.id.file_size);
        timeStampTextView = itemView.findViewById(R.id.timestamp);
        setUpTintedDrawable(getStyle());
        fileSizeTextView.setTextColor(getColorInt(getStyle().mediaAndFilesTextColor));
        fileHeaderTextView.setTextColor(getColorInt(getStyle().mediaAndFilesTextColor));
        timeStampTextView.setTextColor(getColorInt(getStyle().mediaAndFilesTextColor));
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
        setUpDownloadButton(mDownloadButton);
        if (fileDescription.getFileUri() == null) {
            mDownloadButton.setVisibility(View.VISIBLE);
            mImageButton.setVisibility(View.GONE);
            mDownloadButton.setOnClickListener(fileDownloadListener);
        } else {
            mDownloadButton.setVisibility(View.GONE);
            mImageButton.setVisibility(View.VISIBLE);
        }
        mDownloadButton.setProgress(fileDescription.getFileUri() != null ? 100 : fileDescription.getDownloadProgress());
        if (FileUtils.isImage(fileDescription) && fileDescription.getPreviewFileDescription() != null) {
            String downloadPath = "";
            if (fileDescription.getPreviewFileDescription().getFileUri() != null) {
                downloadPath = fileDescription.getPreviewFileDescription().getFileUri().toString();
            } else if (fileDescription.getDownloadPath() != null) {
                downloadPath = fileDescription.getPreviewFileDescription().getDownloadPath();
            }
            ImageLoader
                    .get()
                    .load(downloadPath)
                    .scales(ImageView.ScaleType.FIT_XY)
                    .into(mImageButton);
        } else {
            mImageButton.setImageDrawable(tintedDrawable);
        }
        fileNameFromDescription(fileDescription, this::setFileHeaderTextName);
        long fileSize = fileDescription.getSize();
        fileSizeTextView.setText(FileUtilsKt.toFileSize(fileSize));
        fileSizeTextView.setVisibility(fileSize > 0 ? View.VISIBLE : View.GONE);
        timeStampTextView.setText(sdf.format(new Date(fileDescription.getTimeStamp())));
        mImageButton.setOnClickListener(fileClickListener);
        ViewGroup vg = (ViewGroup) itemView;
        for (int i = 0; i < vg.getChildCount(); i++) {
            vg.getChildAt(i).setOnClickListener(fileClickListener);
        }
    }

    public Unit setFileHeaderTextName(String fileName) {
        fileHeaderTextView.setText(fileName);
        return Unit.INSTANCE;
    }

    private void setUpDownloadButton(CircularProgressButton button) {
        int downloadButtonTintResId = getStyle().downloadButtonTintResId;
        Drawable startDownload = setUpDrawable(getStyle().startDownloadIconResId, downloadButtonTintResId);
        Drawable inProgress = setUpDrawable(getStyle().inProgressIconResId, downloadButtonTintResId);
        Drawable completed = setUpDrawable(getStyle().completedIconResId, downloadButtonTintResId);
        button.setStartDownloadDrawable(startDownload);
        button.setInProgress(inProgress);
        button.setCompletedDrawable(completed);
        button.setBackgroundColorResId(getStyle().downloadButtonBackgroundTintResId);
    }

    private Drawable setUpDrawable(int iconResId, int colorRes) {
        Drawable drawable = AppCompatResources.getDrawable(context, iconResId);
        if (drawable != null) drawable = drawable.mutate();
        ColorsHelper.setDrawableColor(context, drawable, colorRes);
        return drawable;
    }
}