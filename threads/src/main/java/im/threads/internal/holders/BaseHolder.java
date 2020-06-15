package im.threads.internal.holders;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import im.threads.R;
import im.threads.internal.views.CircularProgressButton;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class BaseHolder extends RecyclerView.ViewHolder {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    BaseHolder(View itemView) {
        super(itemView);
    }

    @ColorInt
    int getColorInt(@ColorRes int colorRes) {
        return ContextCompat.getColor(itemView.getContext(), colorRes);
    }

    void setTextColorToViews(TextView[] views, @ColorRes int colorRes) {
        for (TextView tv :views) {
            tv.setTextColor(getColorInt(colorRes));
        }
    }

    void setTintToViews(Drawable[] views, @ColorRes int colorRes) {
        for (Drawable tv :views) {
            tv.setColorFilter(getColorInt(colorRes), PorterDuff.Mode.SRC_ATOP);
        }
    }

    void setTintToProgressButtonUser(CircularProgressButton button, @ColorRes int colorRes, @ColorRes int colorResInsideCircle){
        Drawable completed = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.file_image_user);
        Drawable inProgress = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_clear_blue_user_36dp);
        Drawable download = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_vertical_align_bottom_user_24dp);
        setTintToViews(new Drawable[]{completed}, colorRes);
        setTintToViews(new Drawable[]{inProgress, download}, colorResInsideCircle);
        button.setCompletedDrawable(completed);
        button.setStartDownloadDrawable(download);
        button.setInProgress(inProgress);
    }

    void setTintToProgressButtonConsult(CircularProgressButton button, @ColorRes int colorRes){
        Drawable completed = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.file_image_consult);
        Drawable inProgress = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_clear_blue_consult_36dp);
        Drawable download = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_vertical_align_bottom_consult_24dp);
        setTintToViews(new Drawable[]{completed, inProgress, download}, colorRes);
        button.setCompletedDrawable(completed);
        button.setStartDownloadDrawable(download);
        button.setInProgress(inProgress);
    }

    protected boolean subscribe(final Disposable event) {
        if (compositeDisposable == null || compositeDisposable.isDisposed()) {
            compositeDisposable = new CompositeDisposable();
        }
        return compositeDisposable.add(event);
    }

    public void onClear() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
            compositeDisposable = null;
        }
    }
}
