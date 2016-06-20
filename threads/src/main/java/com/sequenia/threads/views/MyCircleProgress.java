package com.sequenia.threads.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.sequenia.threads.R;

/**
 *
 */
public class MyCircleProgress extends View {
    private Paint finishedPaint;
    private Paint innerCirclePaint;
    protected Paint textPaint;
    private RectF finishedOuterRect = new RectF();
    private int progress = 0;
    private int max = 100;
    private int finishedStrokeColor;
    private float finishedStrokeWidth;

    private final int min_size;

    public MyCircleProgress(Context context) {
        this(context, null);
    }

    public MyCircleProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyCircleProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        finishedStrokeWidth=getContext().getResources().getDisplayMetrics().density;
        min_size = 100;
        initPainters();
    }

    protected void initPainters() {
        textPaint = new TextPaint();
        finishedPaint = new Paint();
        finishedPaint.setColor(getContext().getResources().getColor(R.color.orange));
        finishedPaint.setStyle(Paint.Style.STROKE);
        finishedPaint.setAntiAlias(true);
        finishedPaint.setStrokeWidth(finishedStrokeWidth);

        innerCirclePaint = new Paint();
        innerCirclePaint.setAntiAlias(true);
        this.setRotation(-90.0f);
    }


    @Override
    public void invalidate() {
        initPainters();
        super.invalidate();
    }

    public float getFinishedStrokeWidth() {
        return finishedStrokeWidth;
    }

    public void setFinishedStrokeWidth(float finishedStrokeWidth) {
        this.finishedStrokeWidth = finishedStrokeWidth;
        this.invalidate();
    }

    private float getProgressAngle() {
        return getProgress() / (float) max * 360f;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        if (this.progress > getMax()) {
            this.progress %= getMax();
        }
        invalidate();
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        if (max > 0) {
            this.max = max;
            invalidate();
        }
    }


    public int getFinishedStrokeColor() {
        return finishedStrokeColor;
    }

    public void setFinishedStrokeColor(int finishedStrokeColor) {
        this.finishedStrokeColor = finishedStrokeColor;
        this.invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec));
    }

    private int measure(int measureSpec) {
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = min_size;
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(result, size);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        finishedOuterRect.set(finishedStrokeWidth,
                finishedStrokeWidth,
                getWidth() - finishedStrokeWidth,
                getHeight() - finishedStrokeWidth);
        canvas.drawArc(finishedOuterRect, 0, -getProgressAngle(), false, finishedPaint);
    }
}
