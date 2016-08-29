package com.sequenia.threads.views;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.sequenia.threads.R;
import com.sequenia.threads.utils.SwipeListener;

/**
 * Created by yuri on 09.08.2016.
 */
public class MySwipeRefreshLayout extends SwipeRefreshLayout {
    private static final String MY_TAG = "MySwipeRefreshLayout";
    private float initX;
    private float initY;
    private int mActivePointerId;
    int mTouchSlop;
    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_Y_MAX_DISTANCE = 500;
    private static final int SWIPE_X_MIN_DISTANCE = 350;
    private static final float SWIPE_MIN_VELOCITY = 4.5f;
    private boolean isSwiping;
    private VelocityTracker mVelocityTracker;
    private SwipeListener mSwipeListener;
    private RecyclerView nestedRecyclerview;

    public MySwipeRefreshLayout(Context context) {
        super(context);
    }

    public MySwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return !isRefreshing() && super.onStartNestedScroll(child, target, nestedScrollAxes);
    }

    @Override
    public boolean canChildScrollUp() {
        if (isRefreshing()) {
            return true;
        }
        return false;
    }

    private static final String TAG = "RefreshTag";
    private boolean selfCancelled = false;


    @Override
    protected Parcelable onSaveInstanceState() {
        if (isRefreshing()) {
            clearAnimation();
            setRefreshing(false);
            selfCancelled = true;
        }
        return super.onSaveInstanceState();
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        super.setRefreshing(refreshing);
        selfCancelled = false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus && selfCancelled) {
            setRefreshing(true);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (nestedRecyclerview == null)
            nestedRecyclerview = (RecyclerView) findViewById(R.id.recycler);
        boolean superb = super.onInterceptTouchEvent(ev);
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                initX = ev.getX();
                initY = ev.getY();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mVelocityTracker = VelocityTracker.obtain();
                mVelocityTracker.addMovement(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(ev);
                mVelocityTracker.computeCurrentVelocity(1);
                float xDistance =  Math.abs(initX - ev.getX());
                float yDistance = Math.abs(initY - ev.getY());
                float velocityX = mVelocityTracker.getXVelocity(ev.getPointerId(0));
                float velocityY = mVelocityTracker.getYVelocity(ev.getPointerId(0));
                isSwiping = false;
                if (Math.abs(velocityX) > 1 && Math.abs(velocityY) < 1 && yDistance < 50 && (initX < ev.getX())) {
                    isSwiping = true;
                    if (mSwipeListener != null) mSwipeListener.onSwipe();
                    mVelocityTracker.clear();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (initX == -1 || initY == -1 || mActivePointerId == -1) {
                    mVelocityTracker.clear();
                    break;
                }
                xDistance = Math.abs(initX - ev.getX());
                yDistance = Math.abs(initY - ev.getY());
                if (xDistance < SWIPE_X_MIN_DISTANCE || yDistance > SWIPE_Y_MAX_DISTANCE) {
                    mVelocityTracker.clear();
                    break;
                }
                mVelocityTracker.computeCurrentVelocity(1);
                velocityX = mVelocityTracker.getXVelocity(ev.getPointerId(0));
                velocityY = mVelocityTracker.getYVelocity(ev.getPointerId(0));
                if (velocityX > SWIPE_MIN_VELOCITY && xDistance > SWIPE_MIN_DISTANCE && velocityY < 1) {
                    if (initX < ev.getX()) // left to  right
                    {
                        if (mSwipeListener != null) mSwipeListener.onSwipe();
                    }
                }
                mVelocityTracker.clear();
                break;
        }
        return superb;
    }


    public void setmSwipeListener(SwipeListener mSwipeListener) {
        this.mSwipeListener = mSwipeListener;
    }

    public interface SwipeListener {
        void onSwipe();
    }
}
