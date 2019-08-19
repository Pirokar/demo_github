package im.threads.internal.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import im.threads.internal.picasso_url_connection_only.Picasso;
import im.threads.internal.picasso_url_connection_only.Target;

public abstract class TargetNoError implements Target {
    @Override
    public abstract void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from);

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
    }
}
