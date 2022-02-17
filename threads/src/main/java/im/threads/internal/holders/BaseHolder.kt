package im.threads.internal.holders

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import im.threads.R
import im.threads.internal.model.ErrorStateEnum
import im.threads.internal.views.CircularProgressButton
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var compositeDisposable: CompositeDisposable? = CompositeDisposable()

    @ColorInt
    fun getColorInt(@ColorRes colorRes: Int): Int {
        return ContextCompat.getColor(itemView.context, colorRes)
    }

    fun setTextColorToViews(views: Array<TextView>, @ColorRes colorRes: Int) {
        for (tv in views) {
            tv.setTextColor(getColorInt(colorRes))
        }
    }

    fun setTintToViews(views: Array<Drawable?>, @ColorRes colorRes: Int) {
        for (tv in views) {
            tv?.setColorFilter(getColorInt(colorRes), PorterDuff.Mode.SRC_ATOP)
        }
    }

    fun setTintToProgressButtonUser(
        button: CircularProgressButton,
        @ColorRes colorRes: Int
    ) {
        val completed = AppCompatResources.getDrawable(
            itemView.context,
            R.drawable.file_image_user
        )?.mutate()
        val inProgress = AppCompatResources.getDrawable(
            itemView.context,
            R.drawable.ic_clear_blue_user_36dp
        )?.mutate()
        val download = AppCompatResources.getDrawable(
            itemView.context,
            R.drawable.ic_vertical_align_bottom_user_24dp
        )?.mutate()
        setTintToViews(arrayOf(completed, inProgress, download), colorRes)
        button.setCompletedDrawable(completed)
        button.setStartDownloadDrawable(download)
        button.setInProgress(inProgress)
    }

    fun setTintToProgressButtonConsult(button: CircularProgressButton, @ColorRes colorRes: Int) {
        val completed = AppCompatResources.getDrawable(
            itemView.context,
            R.drawable.file_image_consult
        )?.mutate()
        val inProgress = AppCompatResources.getDrawable(
            itemView.context,
            R.drawable.ic_clear_blue_consult_36dp
        )?.mutate()
        val download = AppCompatResources.getDrawable(
            itemView.context,
            R.drawable.ic_vertical_align_bottom_consult_24dp
        )?.mutate()
        setTintToViews(arrayOf(completed, inProgress, download), colorRes)
        button.setCompletedDrawable(completed)
        button.setStartDownloadDrawable(download)
        button.setInProgress(inProgress)
    }

    protected fun getErrorImageResByErrorCode(code: ErrorStateEnum): Int {
        when (code) {
            ErrorStateEnum.DISALLOWED -> return R.drawable.im_wrong_file
            ErrorStateEnum.TIMEOUT -> return R.drawable.im_unexpected
            ErrorStateEnum.Unexpected -> return R.drawable.im_unexpected
            ErrorStateEnum.ANY -> return R.drawable.im_unexpected
            else -> return return 0
        }
    }

    protected fun subscribe(event: Disposable): Boolean {
        if (compositeDisposable?.isDisposed != false) {
            compositeDisposable = CompositeDisposable()
        }
        return compositeDisposable?.add(event) ?: false
    }

    fun onClear() {
        compositeDisposable?.apply {
            dispose()
        }
        compositeDisposable = null
    }
}