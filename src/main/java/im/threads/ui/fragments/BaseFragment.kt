package im.threads.ui.fragments

import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseFragment() : Fragment() {
    private var compositeDisposable: CompositeDisposable? = null
    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeAll()
    }

    protected fun subscribe(event: Disposable?): Boolean {
        if (compositeDisposable == null || compositeDisposable?.isDisposed == true) {
            compositeDisposable = CompositeDisposable()
        }
        return if (event != null) {
            compositeDisposable?.add(event) ?: false
        } else {
            false
        }
    }

    private fun unsubscribeAll() {
        if (compositeDisposable != null) {
            compositeDisposable?.dispose()
            compositeDisposable = null
        }
    }
}
