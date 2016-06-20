package com.sequenia.threads.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.sequenia.threads.R;


/**
 *
 */
public class CircularProgressButton extends FrameLayout {
    private MyCircleProgress mcp;
    private View mImageLabel;
    private View background;
    private Drawable completedDrawable;
    private Drawable inProgress;
    private Drawable startDownloadDrawable;

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
        mImageLabel.setBackground(startDownloadDrawable);
        background = findViewById(R.id.background);
        ta.recycle();
    }

    public void setProgress(int progress) {
        mcp.setProgress(progress);
        if (progress > 0 && (progress != 100)) {
            mcp.setVisibility(VISIBLE);
            background.setVisibility(VISIBLE);
            mImageLabel.setBackground(inProgress);
        } else if (progress > 99) {
            mcp.setVisibility(INVISIBLE);
            mImageLabel.setVisibility(INVISIBLE);
            background.setVisibility(INVISIBLE);
            this.setBackground(completedDrawable);
        } else if (progress == 0) {
            mImageLabel.setBackground(startDownloadDrawable);
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
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setOnClickListener(ocl);
        }
    }
}

