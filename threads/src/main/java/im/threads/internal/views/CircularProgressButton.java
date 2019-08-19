package im.threads.internal.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import im.threads.R;
import im.threads.internal.utils.ViewUtils;

public class CircularProgressButton extends FrameLayout {
    private MyCircleProgress mcp;
    private View mImageLabel;
    private View background;
    private Drawable completedDrawable;
    private Drawable inProgress;
    private Drawable startDownloadDrawable;
    private Drawable progressBackgroundDrawable;

    public CircularProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircularProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_progress_button, this, true);
        mcp = findViewById(R.id.circular_progress);
        mImageLabel = findViewById(R.id.label_image);
        TypedArray ta = getContext().getTheme().obtainStyledAttributes(
                attributeSet,
                R.styleable.CircularProgressButton,
                0, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            completedDrawable = ta.getDrawable(R.styleable.CircularProgressButton_completed_drawable);
            inProgress = ta.getDrawable(R.styleable.CircularProgressButton_in_progress_label);
            startDownloadDrawable = ta.getDrawable(R.styleable.CircularProgressButton_start_download_label);
        } else {
            final int completedDrawableId = ta.getResourceId(R.styleable.CircularProgressButton_completed_drawable, -1);
            final int inProgressId = ta.getResourceId(R.styleable.CircularProgressButton_in_progress_label, -1);
            final int startDownloadDrawableId = ta.getResourceId(R.styleable.CircularProgressButton_start_download_label, -1);
            if (completedDrawableId != -1)
                completedDrawable = AppCompatResources.getDrawable(context, completedDrawableId);
            if (inProgressId != -1)
                inProgress = AppCompatResources.getDrawable(context, inProgressId);
            if (startDownloadDrawableId != -1)
                startDownloadDrawable = AppCompatResources.getDrawable(context, startDownloadDrawableId);
        }
        background = findViewById(R.id.background);
        ta.recycle();
        mImageLabel.setVisibility(View.VISIBLE);
        mcp.setVisibility(VISIBLE);
        background.setVisibility(VISIBLE);
        mImageLabel.setBackground(startDownloadDrawable);
        progressBackgroundDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.circle_gray_48dp);
        background.setBackground(progressBackgroundDrawable);
        this.setBackground(null);
    }

    public void setProgress(int progress) {
        mcp.setProgress(progress);
        if (progress > 0 && progress < 100) {
            if (mcp.getVisibility() == INVISIBLE) mcp.setVisibility(VISIBLE);
            if (background.getVisibility() == INVISIBLE) background.setVisibility(VISIBLE);
            if (background.getBackground() == null
                    || !background.getBackground().equals(progressBackgroundDrawable)) {
                background.setBackground(progressBackgroundDrawable);
            }
            if (mImageLabel.getBackground() == null
                    || !mImageLabel.getBackground().equals(inProgress)) {
                mImageLabel.setBackground(inProgress);
            }
            if (getBackground() != null) {
                setBackground(null);
            }
        } else if (progress > 99) {
            mcp.setVisibility(INVISIBLE);
            mImageLabel.setVisibility(INVISIBLE);
            background.setVisibility(INVISIBLE);
            this.setBackground(completedDrawable);
        } else if (progress == 0) {
            mImageLabel.setVisibility(View.VISIBLE);
            mcp.setVisibility(VISIBLE);
            background.setVisibility(VISIBLE);
            mImageLabel.setBackground(startDownloadDrawable);
            this.setBackground(null);
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
        ViewUtils.setClickListener(findViewById(R.id.frame), ocl);
    }

    public void setBackgroundColorResId(@ColorRes int colorResourceIntId) {
        progressBackgroundDrawable.setColorFilter(ContextCompat.getColor(this.getContext(), colorResourceIntId), PorterDuff.Mode.SRC_ATOP);
    }
}
