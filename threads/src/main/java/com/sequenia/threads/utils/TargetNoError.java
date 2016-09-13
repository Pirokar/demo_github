package com.sequenia.threads.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.sequenia.threads.picasso_url_connection_only.Picasso;
import com.sequenia.threads.picasso_url_connection_only.Target;

/**
 * Created by yuri on 13.09.2016.
 */
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
