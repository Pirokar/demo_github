package im.threads.internal.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;
import im.threads.internal.Config;

public final class MyCircleProgress extends View {
    private Paint finishedPaint;
    private RectF finishedOuterRect = new RectF();
    private int progress = 0;
    private int max = 100;
    private float finishedStrokeSize;
    private final int minSize;

    public MyCircleProgress(Context context) {
        this(context, null);
    }

    public MyCircleProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyCircleProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        finishedStrokeSize = getContext().getResources().getDisplayMetrics().density;
        minSize = 100;
        initPaint();
    }

    @Override
    public void invalidate() {
        initPaint();
        super.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        finishedOuterRect.set(finishedStrokeSize,
                finishedStrokeSize,
                getWidth() - finishedStrokeSize,
                getHeight() - finishedStrokeSize);
        canvas.drawArc(finishedOuterRect, 0, -getProgressAngle(), false, finishedPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec));
    }

    void setProgress(int progress) {
        this.progress = progress;
        if (this.progress > max) {
            this.progress %= max;
        }
        invalidate();
    }

    private int measure(int measureSpec) {
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = minSize;
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(result, size);
            }
        }
        return result;
    }

    private float getProgressAngle() {
        return progress / (float) max * 360f;
    }

    private void initPaint() {
        finishedPaint = new Paint();
        finishedPaint.setColor(ContextCompat.getColor(getContext(), Config.instance.getChatStyle().chatBodyIconsTint));
        finishedPaint.setStyle(Paint.Style.STROKE);
        finishedPaint.setAntiAlias(true);
        finishedPaint.setStrokeWidth(finishedStrokeSize);
        this.setRotation(-90.0f);
    }
}
