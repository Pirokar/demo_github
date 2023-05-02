package im.threads.ui.views;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import im.threads.R;

public final class MySwipeRefreshLayout extends SwipeRefreshLayout {
    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_Y_MAX_DISTANCE = 500;
    private static final int SWIPE_X_MIN_DISTANCE = 350;
    private static final float SWIPE_MIN_VELOCITY = 4.5f;
    int mTouchSlop;
    private float initX;
    private float initY;
    private int mActivePointerId;
    private VelocityTracker mVelocityTracker;
    private SwipeListener mSwipeListener;
    private RecyclerView nestedRecyclerView;
    private boolean selfCancelled = false;

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
        return isRefreshing();
    }

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
        if (nestedRecyclerView == null)
            nestedRecyclerView = findViewById(R.id.recycler);
        boolean superb = super.onInterceptTouchEvent(ev);
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                initX = ev.getX();
                initY = ev.getY();
                mActivePointerId = ev.getPointerId(0);
                mVelocityTracker = VelocityTracker.obtain();
                mVelocityTracker.addMovement(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(ev);
                mVelocityTracker.computeCurrentVelocity(1);
                float yDistance = Math.abs(initY - ev.getY());
                float velocityX = mVelocityTracker.getXVelocity(ev.getPointerId(0));
                float velocityY = mVelocityTracker.getYVelocity(ev.getPointerId(0));
                if (Math.abs(velocityX) > 1 && Math.abs(velocityY) < 1 && yDistance < 50 && (initX < ev.getX())) {
                    if (mSwipeListener != null) mSwipeListener.onSwipe();
                    mVelocityTracker.clear();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (initX == -1 || initY == -1 || mActivePointerId == -1) {
                    mVelocityTracker.clear();
                    break;
                }
                float xDistance = Math.abs(initX - ev.getX());
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

    public void setSwipeListener(SwipeListener mSwipeListener) {
        this.mSwipeListener = mSwipeListener;
    }

    public interface SwipeListener {
        void onSwipe();
    }
}
