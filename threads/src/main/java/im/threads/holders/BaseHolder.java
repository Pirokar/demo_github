package im.threads.holders;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import im.threads.R;
import im.threads.views.CircularProgressButton;

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
    protected void setTintToProgressButtonUser(CircularProgressButton button, @ColorRes int colorRes, @ColorRes int colorResInsideCurcle){
        Drawable completed = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.file_image_user);
        Drawable inProgress = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_clear_blue_user_36dp);
        Drawable download = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_vertical_align_bottom_user_24dp);
        setTintToViews(new Drawable[]{completed}, colorRes);
        setTintToViews(new Drawable[]{inProgress, download}, colorResInsideCurcle);
        button.setCompletedDrawable(completed);
        button.setStartDownloadDrawable(download);
        button.setInProgress(inProgress);
    }

    protected void setTintToProgressButtonConsult(CircularProgressButton button, @ColorRes int colorRes){

        Drawable completed = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.file_image_consult);
        Drawable inProgress = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_clear_blue_consult_36dp);
        Drawable download = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_vertical_align_bottom_consult_24dp);
        setTintToViews(new Drawable[]{completed, inProgress, download}, colorRes);
        button.setCompletedDrawable(completed);
        button.setStartDownloadDrawable(download);
        button.setInProgress(inProgress);
    }
}
