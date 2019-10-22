package im.threads.internal.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;

import im.threads.internal.picasso_url_connection_only.Transformation;

public final class MaskedTransformation implements Transformation {

    private Drawable maskDrawable;
    private String cacheKey;

    public MaskedTransformation(Drawable maskDrawable) {
        this.maskDrawable = maskDrawable;
        cacheKey = String.valueOf(maskDrawable.hashCode());
    }

    @Override
    public Bitmap transform(Bitmap source) {
        Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(result);
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inSampleSize = 2;

        Bitmap mask = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas tmpCanvas = new Canvas(mask);
        NinePatchDrawable maskRaw9Patch = (NinePatchDrawable) maskDrawable;
        maskRaw9Patch.setBounds(0, 0, tmpCanvas.getWidth(), tmpCanvas.getHeight());
        maskRaw9Patch.draw(tmpCanvas);
        
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mCanvas.drawBitmap(source, 0, 0, null);
        mCanvas.drawBitmap(mask, 0, 0, paint);
        paint.setXfermode(null);
        source.recycle();
        return result;
    }

    @Override
    public String key() {
        return cacheKey;
    }
}
