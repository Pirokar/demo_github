package im.threads.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import im.threads.R;
import im.threads.utils.ViewUtils;


/**
 *
 */
public class CircularProgressButton extends FrameLayout {
    private static final String TAG = "CircularProgressButton ";
    private MyCircleProgress mcp;
    private View mImageLabel;
    private View background;
    private Drawable completedDrawable;
    private Drawable inProgress;
    private Drawable startDownloadDrawable;
    private Drawable progressBackgroundDrawable;

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
        mcp = (MyCircleProgress) findViewById(R.id.circular_progress);
        mImageLabel = findViewById(R.id.label_image);
        TypedArray ta = getContext().getTheme().obtainStyledAttributes(
                attributeSet,
                R.styleable.CircularProgressButton,
                0, 0);
        completedDrawable = ta.getDrawable(R.styleable.CircularProgressButton_completed_drawable);
        inProgress = ta.getDrawable(R.styleable.CircularProgressButton_in_progress_label);
        startDownloadDrawable = ta.getDrawable(R.styleable.CircularProgressButton_start_download_label);
        background = findViewById(R.id.background);
        ta.recycle();
        mImageLabel.setVisibility(View.VISIBLE);
        mcp.setVisibility(VISIBLE);
        background.setVisibility(VISIBLE);
        mImageLabel.setBackground(startDownloadDrawable);
        progressBackgroundDrawable = ContextCompat.getDrawable(getContext(), R.drawable.circle_gray_48dp);
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

    public int getProgress() {
        return mcp.getProgress();
    }


    public View getImageLabel() {
        return mImageLabel;
    }

    public void setImageLabel(View mImageLabel) {
        this.mImageLabel = mImageLabel;
    }

    public Drawable getCompletedDrawable() {
        return completedDrawable;
    }

    public void setCompletedDrawable(Drawable completedDrawable) {
        this.completedDrawable = completedDrawable;
    }

    public Drawable getInProgress() {
        return inProgress;
    }

    public void setInProgress(Drawable inProgress) {
        this.inProgress = inProgress;
    }

    public Drawable getStartDownloadDrawable() {
        return startDownloadDrawable;
    }

    public void setStartDownloadDrawable(Drawable startDownloadDrawable) {
        this.startDownloadDrawable = startDownloadDrawable;
    }

    public void setOnClickListener(View.OnClickListener ocl) {
        ViewUtils.setClickListener((ViewGroup) findViewById(R.id.frame), ocl);
    }

    public void setBackgroundColor(@ColorRes int colorResourceIntId) {
        progressBackgroundDrawable.setColorFilter(ContextCompat.getColor(this.getContext(), colorResourceIntId), PorterDuff.Mode.SRC_ATOP);
    }
}
