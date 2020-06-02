/*
 * Copyright (C) 2016 yydcdut (yuyidong2015@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yydcdut.markdown.span;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.style.DynamicDrawableSpan;
import android.view.View;

import androidx.annotation.NonNull;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.yydcdut.markdown.drawable.ForwardingDrawable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * image grammar span
 * <p>
 * Created by yuyidong on 16/5/16.
 */
public class MDImageSpan extends DynamicDrawableSpan {

    private static Pattern sImageUrlPattern = Pattern.compile("^(.*?)/(\\d+)\\$(\\d+)$");
    private final ForwardingDrawable mActualDrawable;
    private String mImageUri;
    private Drawable mPlaceHolder;
    private Drawable mFinalDrawable;
    private boolean isAttached;
    private boolean isDetached;
    private View mAttachedView;
    private boolean mIsRequestSubmitted = false;

    /**
     * Constructor
     *
     * @param uri           the image url
     * @param width         the display width
     * @param height        the display height
     */
    public MDImageSpan(String uri, int width, int height) {
        super(ALIGN_BOTTOM);
        mImageUri = uri;
        int[] size = getSize(uri, width, height);
        mPlaceHolder = createEmptyDrawable(size[0], size[1]);
        mActualDrawable = new ForwardingDrawable(mPlaceHolder);
        Rect bounds = mPlaceHolder.getBounds();
        if (bounds.right == 0 || bounds.bottom == 0) {
            mActualDrawable.setBounds(0, 0, mPlaceHolder.getIntrinsicWidth(), mPlaceHolder.getIntrinsicHeight());
        } else {
            mActualDrawable.setBounds(bounds);
        }
    }

    @NonNull
    private static String getUrl(String sourceUrl) {
        Matcher m = sImageUrlPattern.matcher(sourceUrl);
        if (m.find()) {
            String url = m.group(1);
            if (url != null) {
                return url;
            }
        }
        return sourceUrl;
    }

    private Drawable createEmptyDrawable(int width, int height) {
        ColorDrawable d = new ColorDrawable(Color.TRANSPARENT);
        d.setBounds(0, 0, width, height);
        return d;
    }

    @NonNull
    private int[] getSize(String sourceUrl, int defaultWidth, int defaultHeight) {
        Matcher m = sImageUrlPattern.matcher(sourceUrl);
        int[] size = new int[]{defaultWidth, defaultHeight};
        if (m.find()) {
            String sizeStr = m.group(2);
            if (sizeStr != null && TextUtils.isDigitsOnly(sizeStr)) {
                size[0] = Integer.parseInt(sizeStr);
            }
            sizeStr = m.group(3);
            if (sizeStr != null && TextUtils.isDigitsOnly(sizeStr)) {
                size[1] = Integer.parseInt(sizeStr);
            }
        }
        return size;
    }

    @Override
    public Drawable getDrawable() {
        return mActualDrawable;
    }

    /**
     * invoke when view created
     *
     * @param view the view
     */
    public void onAttach(@NonNull View view) {
        isAttached = true;
        if (mAttachedView != view) {
            mActualDrawable.setCallback(null);
            if (mAttachedView != null) {
                throw new IllegalStateException("has been attached to view:" + mAttachedView);
            }
            mAttachedView = view;
            mActualDrawable.setCallback(mAttachedView);
        }
        loadImage();
    }

    private void loadImage() {
        Picasso.get()
                .load(getUrl(mImageUri))
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        setImageWithIntrinsicBounds(createBitmapDrawable(bitmap));
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }

    private void setImageWithIntrinsicBounds(@NonNull Drawable drawable) {
        if (mFinalDrawable != drawable) {
            mActualDrawable.setCurrent(drawable);
            mFinalDrawable = drawable;
        }
    }

    private BitmapDrawable createBitmapDrawable(Bitmap bitmap) {
        if (mAttachedView != null) {
            final Context context = mAttachedView.getContext();
            if (context != null) {
                return new BitmapDrawable(context.getResources(), bitmap);
            }
        }
        return new BitmapDrawable(null, bitmap);
    }

    private int calculateSampleSize(@NonNull BitmapFactory.Options options, int expectWidth, int expectHeight) {
        int sampleSize = 1;
        while (options.outHeight / sampleSize > expectWidth || options.outWidth / sampleSize > expectHeight) {
            sampleSize = sampleSize << 1;
        }
        return sampleSize;
    }

    private Drawable getDrawable(@NonNull byte[] bytes) {
        BitmapFactory.Options calculateOptions = new BitmapFactory.Options();
        calculateOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, calculateOptions);
        int expectWidth = mActualDrawable.getIntrinsicWidth();
        int expectHeight = mActualDrawable.getIntrinsicHeight();
        int sampleSize = 1;
        if (expectWidth >= 0 && expectHeight >= 0) {
            sampleSize = calculateSampleSize(calculateOptions, expectWidth, expectHeight);
        } else if (mPlaceHolder.getBounds().width() >= 0 && mPlaceHolder.getBounds().height() >= 0) {
            Rect rect = mPlaceHolder.getBounds();
            sampleSize = calculateSampleSize(calculateOptions, rect.width(), rect.height());
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        return createBitmapDrawable(bitmap);
    }

    public void onDetach() {
        isDetached = true;
        if (!isAttached) {
            return;
        }
        mActualDrawable.setCallback(null);
        mAttachedView = null;
        mActualDrawable.setCurrent(mPlaceHolder);
    }

}
