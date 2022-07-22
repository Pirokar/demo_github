package im.threads.android.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import im.threads.internal.utils.ThreadsLogger
import java.util.concurrent.TimeUnit

class LocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = TimeUnit.SECONDS.toMillis(60)
        fastestInterval = TimeUnit.SECONDS.toMillis(30)
        maxWaitTime = TimeUnit.MINUTES.toMillis(2)
    }

    private val locationUpdatePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, LocationBroadcastReceiver::class.java)
        intent.action = LocationBroadcastReceiver.ACTION_PROCESS_UPDATES
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationUpdatePendingIntent)
        } catch (permissionRevoked: SecurityException) {
            ThreadsLogger.e(TAG, "Location permission revoked; details: $permissionRevoked")
            throw permissionRevoked
        }
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationUpdatePendingIntent)
    }

    companion object {
        private const val TAG = "LocationManager"
    }
}