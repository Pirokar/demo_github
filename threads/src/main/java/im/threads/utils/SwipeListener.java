package im.threads.utils;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import im.threads.internal.ThreadsLogger;

/**
 * Created by yuri on 25.08.2016.
 */
public class SwipeListener implements View.OnTouchListener {
    private static final String TAG = "SwipeListener ";
    private GestureDetector mGestureDetector;
    private MySwipeListener listener;

    public SwipeListener(Context ctx, MySwipeListener listener) {
        mGestureDetector = new GestureDetector(ctx, new MyGestureDetector());
        this.listener = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_MIN_DISTANCE = 100;
        private static final int SWIPE_Y_MAX_DISTANCE = 600;
        private static final int SWIPE_X_MAX_DISTANCE = 600;
        private static final int SWIPE_X_MIN_DISTANCE = 350;
        private static final int SWIPE_MIN_VELOCITY = 80;


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) return false;
            ThreadsLogger.e(TAG, "onFling");
            final float xDistance = Math.abs(e1.getX() - e2.getX());
            final float yDistance = Math.abs(e1.getY() - e2.getY());
            if (xDistance < SWIPE_X_MIN_DISTANCE || yDistance > SWIPE_Y_MAX_DISTANCE) {
                return false;
            }
            velocityX = Math.abs(velocityX);
            velocityY = Math.abs(velocityY);
            if (velocityX > SWIPE_MIN_VELOCITY && xDistance > SWIPE_MIN_DISTANCE) {
                if (e1.getX() < e2.getX()) // left to  right
                    if (null != listener) {
                        listener.onRightSwipe();
                        return true;
                    }
            }
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
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }
    }

    public interface MySwipeListener {
        void onRightSwipe();
    }
}
