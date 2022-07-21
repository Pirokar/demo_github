package im.threads.android.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationResult
import im.threads.internal.Config

class LocationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_PROCESS_UPDATES) {
            LocationAvailability.extractLocationAvailability(intent)?.let { locationAvailability ->
                if (!locationAvailability.isLocationAvailable) {
                    Log.e(TAG, "Location services are no longer available!")
                }
            }

            LocationResult.extractResult(intent)?.let { locationResult ->
                locationResult.locations.map { location ->
                    Config.instance.transport.updateLocation(location.latitude, location.longitude)
                    Log.e(TAG, "###   Location received.   location - " + location)
                }
            }
        }
    }

    companion object {
        private const val TAG = "LocationBR"
        const val ACTION_PROCESS_UPDATES = "com.google.android.gms.location.action.ACTION_PROCESS_UPDATES"
    }
}
