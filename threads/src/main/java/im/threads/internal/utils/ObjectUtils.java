package im.threads.internal.utils;

public class ObjectUtils {

    public static boolean areEqual(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

}
