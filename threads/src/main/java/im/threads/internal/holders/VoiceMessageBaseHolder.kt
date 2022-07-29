package im.threads.internal.holders

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import com.google.android.material.slider.Slider
import im.threads.ChatStyle
import im.threads.internal.model.FileDescription
import im.threads.internal.utils.ColorsHelper

abstract class VoiceMessageBaseHolder internal constructor(itemView: View) :
    BaseHolder(itemView) {

    abstract fun getFileDescription(): FileDescription?

    abstract fun init(maxValue: Int, progress: Int, isPlaying: Boolean)
    abstract fun updateProgress(progress: Int)
    abstract fun updateIsPlaying(isPlaying: Boolean)
    abstract fun resetProgress()

    protected fun initSliderView(context: Context?, style: ChatStyle, slider: Slider, isIncoming: Boolean) {
        val chatImagesColorStateList: ColorStateList
        val fullColorStateListSize = 3
        val alphaForTrack = 100
        val colorResId = if (isIncoming) style.incomingPlayPauseButtonColor else style.outgoingPlayPauseButtonColor
        chatImagesColorStateList = if (style.chatBodyIconsColorState != null &&
            style.chatBodyIconsColorState.size >= fullColorStateListSize
        ) {
            ColorsHelper.getColorStateList(
                context,
                style.chatBodyIconsColorState[0],
                style.chatBodyIconsColorState[1],
                style.chatBodyIconsColorState[2]
            )
        } else {
            ColorsHelper.getSimpleColorStateList(context, colorResId)
        }
        slider.thumbTintList = chatImagesColorStateList
        slider.trackTintList = chatImagesColorStateList.withAlpha(alphaForTrack)
    }
}
