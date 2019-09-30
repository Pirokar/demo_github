package im.threads.internal.utils;

import android.support.v4.util.ObjectsCompat;

public final class Tuple<F, S> {
    public F first;
    public S second;

    public Tuple(F first, S second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple)) return false;

        Tuple<?, ?> pair = (Tuple<?, ?>) o;

        if (!ObjectsCompat.equals(first, pair.first)) return false;
        return ObjectsCompat.equals(second, pair.second);
    }


    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
    }
}

