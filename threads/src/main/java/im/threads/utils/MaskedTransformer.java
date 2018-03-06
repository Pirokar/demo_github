package im.threads.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.NinePatchDrawable;

import im.threads.R;
import im.threads.picasso_url_connection_only.Transformation;

/**
 * Created by yuri on 11.07.2016.
 */
public class MaskedTransformer implements Transformation {
    private static final String TAG = "MaskedTransformer ";
    private static MaskedTransformer instance;
    private Context ctx;
    public static final int TYPE_CONSULT = 1;
    public static final int TYPE_USER = 2;
    int type;

    public MaskedTransformer(Context ctx, int type) {
        this.ctx = ctx;
        this.type = type;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        Bitmap original = source;
        Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(result);
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inSampleSize = 2;

        Bitmap mask = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas tmpCanvas = new Canvas(mask);
        NinePatchDrawable maskRaw9Patch = (NinePatchDrawable)ctx.getResources().getDrawable(type == TYPE_USER ? R.drawable.thread_outgoing_bubble : R.drawable.thread_incoming_bubble);
        maskRaw9Patch.setBounds(0, 0, tmpCanvas.getWidth(), tmpCanvas.getHeight());
        maskRaw9Patch.draw(tmpCanvas);
        
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mCanvas.drawBitmap(original, 0, 0, null);
        mCanvas.drawBitmap(mask, 0, 0, paint);
        paint.setXfermode(null);
        source.recycle();
        return result;
    }

    @Override
    public String key() {
        return "consultBubble";
    }
}
