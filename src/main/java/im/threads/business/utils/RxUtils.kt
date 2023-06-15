package im.threads.business.utils

import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.databinding.ObservableField
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.SingleEmitter

object RxUtils {
    fun <T> toObservable(observableField: ObservableField<T>): Observable<T?> {
        return Observable.create { source: ObservableEmitter<T?> ->
            val callback: OnPropertyChangedCallback = object : OnPropertyChangedCallback() {
                override fun onPropertyChanged(observable: androidx.databinding.Observable, i: Int) {
                    observableField.get()?.let { source.onNext(it) }
                }
            }
            observableField.addOnPropertyChangedCallback(callback)
            source.setCancellable { observableField.removeOnPropertyChangedCallback(callback) }
        }
    }

    fun <T> toObservableImmediately(observableField: ObservableField<T>): Observable<T?> {
        return Observable.create { source: ObservableEmitter<T?> ->
            val callback: OnPropertyChangedCallback = object : OnPropertyChangedCallback() {
                override fun onPropertyChanged(observable: androidx.databinding.Observable, i: Int) {
                    observableField.get()?.let { source.onNext(it) }
                }
            }
            observableField.addOnPropertyChangedCallback(callback)
            source.setCancellable { observableField.removeOnPropertyChangedCallback(callback) }
            val value = observableField.get()
            if (value != null) {
                source.onNext(value)
            }
        }
    }

    fun <T> toSingle(observableField: ObservableField<T>): Single<T?> {
        return Single.create { source: SingleEmitter<T?> ->
            val callback: OnPropertyChangedCallback = object : OnPropertyChangedCallback() {
                override fun onPropertyChanged(observable: androidx.databinding.Observable, i: Int) {
                    observableField.get()?.let { source.onSuccess(it) }
                }
            }
            observableField.addOnPropertyChangedCallback(callback)
            source.setCancellable { observableField.removeOnPropertyChangedCallback(callback) }
        }
    }

    fun <T> toSingleWithImmediateEmission(observableField: ObservableField<T>): Single<T?> {
        return Single.create { source: SingleEmitter<T?> ->
            val callback: OnPropertyChangedCallback = object : OnPropertyChangedCallback() {
                override fun onPropertyChanged(observable: androidx.databinding.Observable, i: Int) {
                    observableField.get()?.let { source.onSuccess(it) }
                }
            }
            observableField.addOnPropertyChangedCallback(callback)
            source.setCancellable { observableField.removeOnPropertyChangedCallback(callback) }
            val value = observableField.get()
            if (value != null) {
                source.onSuccess(value)
            }
        }
    }
}
