package im.threads.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import im.threads.R
import im.threads.ui.ChatStyle
import im.threads.ui.config.Config.Companion.getInstance
import im.threads.ui.utils.ColorsHelper

class Rating : LinearLayout {

    private val style: ChatStyle = getInstance().getChatStyle()
    private var ratingCount = 0
    private var countStars: Int = 0
    private var viewsStar: ArrayList<View> = ArrayList()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun initRating(context: Context, ratingCount: Int, countStars: Int) {
        this.ratingCount = ratingCount
        this.countStars = countStars
        val inflater = LayoutInflater.from(context)
        removeAllViews()
        viewsStar.clear()
        for (i in 0 until countStars) {
            val view = inflater.inflate(R.layout.ecc_rating_star, this, false)
            setImage(view, i < ratingCount)
            viewsStar.add(view)
            addView(view)
        }
    }

    fun setListenerClick(callBackListener: CallBackListener?) {
        if (callBackListener != null) {
            setClickListeners(callBackListener)
        } else {
            deleteClickListeners()
        }
    }

    private fun setClickListeners(callBackListener: CallBackListener) {
        for (i in 0 until countStars) {
            val index = i + 1
            viewsStar[i].setOnClickListener {
                if (isEnabled) {
                    for (j in 0 until countStars) {
                        setImage(viewsStar[j], j < index)
                    }
                    ratingCount = index
                    callBackListener.onStarClick(ratingCount)
                }
            }
        }
    }

    private fun deleteClickListeners() {
        for (i in 0 until countStars) {
            viewsStar[i].setOnClickListener(null)
        }
    }

    private fun setImage(view: View, ratingState: Boolean) {
        val star = view.findViewById<ImageView>(R.id.star)
        if (ratingState) {
            star.setImageResource(style.optionsSurveySelectedIconResId)
            ColorsHelper.setTint(context, star, style.surveySelectedColorFilterResId)
        } else {
            star.setImageResource(style.optionsSurveyUnselectedIconResId)
            if (ratingCount == 0) {
                ColorsHelper.setTint(context, star, style.surveyUnselectedColorFilterResId)
            }
        }
    }

    interface CallBackListener {
        fun onStarClick(ratingCount: Int)
    }
}
