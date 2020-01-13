package im.threads.internal.utils;

import androidx.databinding.ObservableField;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;

public class RxUtils {

    private RxUtils() {
    }

    @NonNull
    public static <T> Observable<T> toObservable(@NonNull final ObservableField<T> observableField) {
        return Observable.create(source -> {
            final androidx.databinding.Observable.OnPropertyChangedCallback callback = new androidx.databinding.Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(androidx.databinding.Observable observable, int i) {
                    source.onNext(observableField.get());
                }
            };
            observableField.addOnPropertyChangedCallback(callback);
            source.setCancellable(() -> observableField.removeOnPropertyChangedCallback(callback));
        });
    }

    public static <T> Single<T> toSingle(@NonNull final ObservableField<T> observableField) {
        return Single.create(source -> {
            final androidx.databinding.Observable.OnPropertyChangedCallback callback =
                    new androidx.databinding.Observable.OnPropertyChangedCallback() {
                        @Override
                        public void onPropertyChanged(androidx.databinding.Observable observable, int i) {
                            source.onSuccess(observableField.get());
                        }
                    };
            observableField.addOnPropertyChangedCallback(callback);
            source.setCancellable(() -> observableField.removeOnPropertyChangedCallback(callback));
        });
    }
}
