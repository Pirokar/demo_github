package im.threads.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import im.threads.model.ChatStyle;

/**
 * Created by yuri on 01.06.2016.
 * view that recognize right swipe
 */
public class SwipeAwareView extends View {
    private static final String TAG = "SwipeAwareView ";
    private SwipeListener mSwipeListener;
    private GestureDetector mGestureDetector;
    private MyGestureDetector mMyGestureDetector;
    private ArrayList<MotionEvent> events = new ArrayList<>(100);


    public SwipeAwareView(Context context) {
        super(context);
        init();
    }

    public SwipeAwareView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwipeAwareView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mGestureDetector = new GestureDetector(this.getContext(), new MyGestureDetector());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        events.add(event);
        return mGestureDetector.onTouchEvent(event);

    }

    public void setSwipeListener(SwipeListener listener) {
        this.mSwipeListener = listener;
    }

    public interface SwipeListener {
        void onRightSwipe();

        boolean onTouchEvent(MotionEvent event);
    }

    private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_MIN_DISTANCE = 100;
        private static final int SWIPE_Y_MAX_DISTANCE = 500;
        private static final int SWIPE_X_MAX_DISTANCE = 600;
        private static final int SWIPE_X_MIN_DISTANCE = 350;
        private static final int SWIPE_MIN_VELOCITY = 100;


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            if (ChatStyle.getInstance().isDebugLoggingEnabled) {
                Log.e(TAG, "onFling");
            }

            boolean answer;
            final float xDistance = Math.abs(e1.getX() - e2.getX());
            final float yDistance = Math.abs(e1.getY() - e2.getY());
            if (xDistance < SWIPE_X_MIN_DISTANCE || yDistance > SWIPE_Y_MAX_DISTANCE) {
                if (mSwipeListener != null) {
                    for (MotionEvent m:events) {
                        mSwipeListener.onTouchEvent(m);
                    }
                }
                events.clear();
                return false;
            }
            velocityX = Math.abs(velocityX);
            velocityY = Math.abs(velocityY);
            if (velocityX > SWIPE_MIN_VELOCITY && xDistance > SWIPE_MIN_DISTANCE) {
                if (e1.getX() < e2.getX()) // left to  right
                    if (null != mSwipeListener) {
                        mSwipeListener.onRightSwipe();
                        events.clear();
                        return true;
                    }
            }
            if (mSwipeListener != null) {
                for (MotionEvent m:events) {
                    mSwipeListener.onTouchEvent(m);
                }
            }
            events.clear();
            return false;
        }

        @Override
        public boolean onContextClick(MotionEvent e) {
            return super.onContextClick(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            super.onShowPress(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }
    }
}