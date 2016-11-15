package com.sequenia.threads.holders;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.sequenia.threads.R;
import com.sequenia.threads.views.CircularProgressButton;

/**
 * Created by yuri on 10.11.2016.
 */

public abstract class BaseHolder extends RecyclerView.ViewHolder {
    public BaseHolder(View itemView) {
        super(itemView);
    }

    protected
    @ColorInt
    int getColorInt(@ColorRes int colorRes) {
        return ContextCompat.getColor(itemView.getContext(), colorRes);
    }
    protected void setTextColorToViews(TextView[] views, @ColorRes int colorRes) {
        for (TextView tv :views) {
            tv.setTextColor(getColorInt(colorRes));
        }
    }
    protected void setTintToViews(Drawable[] views, @ColorRes int colorRes) {
        for (Drawable tv :views) {
            tv.setColorFilter(getColorInt(colorRes), PorterDuff.Mode.SRC_ATOP);
        }
    }
    protected void setTintToProgressButton(CircularProgressButton button, @ColorRes int colorRes){
        Drawable completed = itemView.getResources().getDrawable(R.drawable.ic_insert_file_blue_36dp);
        Drawable inProgress = itemView.getResources().getDrawable(R.drawable.ic_clear_blue_36dp);
        Drawable download = itemView.getResources().getDrawable(R.drawable.ic_vertical_align_bottom_24dp);
        setTintToViews(new Drawable[]{completed, inProgress, download}, colorRes);
        button.setCompletedDrawable(completed);
        button.setStartDownloadDrawable(download);
        button.setInProgress(inProgress);
    }
}
