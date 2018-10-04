package im.threads.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public final class DeviceInfoHelper {

    public static String getOsVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getDeviceName() {
        return Build.MANUFACTURER + " " + Build.MODEL;
    }

    public static String getLocale(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return ctx.getResources().getConfiguration().getLocales().get(0).toLanguageTag();

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return ctx.getResources().getConfiguration().locale.toLanguageTag();

        } else {
            return ctx.getResources().getConfiguration().locale.getLanguage()
                    + "-" + ctx.getResources().getConfiguration().locale.getCountry();
        }
    }

    public static String getIpAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;
                        if (isIPv4)
                            return sAddr;

                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }

    public static boolean hasNoInternet(final Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return null == netInfo || !netInfo.isConnected();
    }
}

