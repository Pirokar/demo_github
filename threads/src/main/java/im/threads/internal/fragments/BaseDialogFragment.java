package im.threads.internal.fragments;

import androidx.fragment.app.DialogFragment;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class BaseDialogFragment extends DialogFragment {

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
}
