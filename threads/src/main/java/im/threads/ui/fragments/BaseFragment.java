package im.threads.ui.fragments;

import androidx.fragment.app.Fragment;

import im.threads.ui.config.Config;
import im.threads.ui.utils.ToastUtils;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class BaseFragment extends Fragment {

    private CompositeDisposable compositeDisposable;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unsubscribeAll();
    }

    protected boolean subscribe(final Disposable event) {
        if (compositeDisposable == null || compositeDisposable.isDisposed()) {
            compositeDisposable = new CompositeDisposable();
        }
        return compositeDisposable.add(event);
    }

    private void unsubscribeAll() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
            compositeDisposable = null;
        }
    }

    public void showToast(final String message) {
        if (Config.getInstance().getChatStyle().isToastStylable())
            ToastUtils.showSnackbar(getContext(), getView().getRootView(), message);
        else {
            ToastUtils.showToast(getContext(), message);
        }
    }
}
