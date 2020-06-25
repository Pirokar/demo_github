package im.threads.internal.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public abstract class TargetNoError implements Target {
    @Override
    public abstract void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from);

    @Override
    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
    }
}
