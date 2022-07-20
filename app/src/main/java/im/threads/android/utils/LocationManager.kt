package im.threads.android.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import java.util.concurrent.TimeUnit

private const val TAG = "JJLocationManager"

class LocationManager private constructor(private val context: Context) {

    private val _receivingLocationUpdates: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = TimeUnit.SECONDS.toMillis(60)
        fastestInterval = TimeUnit.SECONDS.toMillis(30)
        maxWaitTime = TimeUnit.MINUTES.toMillis(2)

        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private val locationUpdatePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, LocationBroadcastReceiver::class.java)
        intent.action = LocationBroadcastReceiver.ACTION_PROCESS_UPDATES
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @Throws(SecurityException::class)
    @MainThread
    fun startLocationUpdates() {
        try {
            _receivingLocationUpdates.value = true
            fusedLocationClient.requestLocationUpdates(locationRequest, locationUpdatePendingIntent)
        } catch (permissionRevoked: SecurityException) {
            _receivingLocationUpdates.value = false
            Log.e(TAG, "Location permission revoked; details: $permissionRevoked")
            throw permissionRevoked
        }
    }

    @MainThread
    fun stopLocationUpdates() {
        _receivingLocationUpdates.value = false
        fusedLocationClient.removeLocationUpdates(locationUpdatePendingIntent)
    }

    companion object {

        private var instance: LocationManager? = null

        fun getInstance(context: Context): LocationManager {
            return instance ?: LocationManager(context).also { instance = it }
        }
    }
}