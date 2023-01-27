package im.threads.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.ColorRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import im.threads.R;
import im.threads.ui.utils.ViewUtils;

public final class CircularProgressButton extends FrameLayout {
    private MyCircleProgress mcp;
    private View mImageLabel;
    private Drawable completedDrawable;
    private Drawable inProgress;
    private Drawable startDownloadDrawable;
    private Drawable progressBackgroundDrawable;
    private final ViewUtils viewUtils = new ViewUtils();

    public CircularProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CircularProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attributeSet) {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_progress_button, this, true);
        mcp = findViewById(R.id.circular_progress);
        mImageLabel = findViewById(R.id.label_image);
        TypedArray ta = getContext().getTheme().obtainStyledAttributes(
                attributeSet,
                R.styleable.CircularProgressButton,
                0, 0);
        completedDrawable = ta.getDrawable(R.styleable.CircularProgressButton_completed_drawable);
        inProgress = ta.getDrawable(R.styleable.CircularProgressButton_in_progress_label);
        startDownloadDrawable = ta.getDrawable(R.styleable.CircularProgressButton_start_download_label);
        View background = findViewById(R.id.background);
        ta.recycle();
        mImageLabel.setVisibility(View.VISIBLE);
        mcp.setVisibility(VISIBLE);
        background.setVisibility(VISIBLE);
        mImageLabel.setBackground(startDownloadDrawable);
        progressBackgroundDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.ecc_circle_gray_48dp);
        background.setBackground(progressBackgroundDrawable);
        this.setBackground(null);
    }

    public void setProgress(int progress) {
        mcp.setProgress(progress);
        if (progress > 0 && progress < 100) {
            mcp.setVisibility(VISIBLE);
            mImageLabel.setBackground(inProgress);
        } else if (progress > 99) {
            mcp.setVisibility(INVISIBLE);
            mImageLabel.setBackground(completedDrawable);
        } else if (progress == 0) {
            mcp.setVisibility(VISIBLE);
            mImageLabel.setBackground(startDownloadDrawable);
        }
    }

    public void setCompletedDrawable(Drawable completedDrawable) {
        this.completedDrawable = completedDrawable;
    }

    public void setInProgress(Drawable inProgress) {
        this.inProgress = inProgress;
    }

    public void setStartDownloadDrawable(Drawable startDownloadDrawable) {
        this.startDownloadDrawable = startDownloadDrawable;
    }

    @Override
    public void setOnClickListener(View.OnClickListener ocl) {
        viewUtils.setClickListener(findViewById(R.id.frame), ocl);
    }

    public void setBackgroundColorResId(@ColorRes int colorResourceIntId) {
        progressBackgroundDrawable.setColorFilter(ContextCompat.getColor(getContext(), colorResourceIntId), PorterDuff.Mode.SRC_ATOP);
    }
}
