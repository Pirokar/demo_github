package com.sequenia.threads.views;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by yuri on 09.08.2016.
 */
public class MySwipeRefreshLayout extends SwipeRefreshLayout {
    public MySwipeRefreshLayout(Context context) {
        super(context);
    }

    public MySwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
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
    protected Parcelable onSaveInstanceState()
    {
        if(isRefreshing()) {
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
        if(hasWindowFocus && selfCancelled) {
            setRefreshing(true);
        }
    }
}
