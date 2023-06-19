package im.threads.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import im.threads.business.useractivity.UserActivityTimeProvider.getLastUserActivityTimeCounter

/**
 * Layout, перехватывающий касания для отслеживания активности пользователя.
 */
class InterceptTouchFrameLayout : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
        super(context, attrs, defStyleAttr)

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (MotionEvent.ACTION_DOWN == ev.action) {
            val timeCounter = getLastUserActivityTimeCounter()
            timeCounter.updateLastUserActivityTime()
        }
        return super.onInterceptTouchEvent(ev)
    }
}
