package im.threads.android.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Handler
import android.os.Looper
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationResult
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna

class LocationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_PROCESS_UPDATES) {
            LocationAvailability.extractLocationAvailability(intent)?.let { locationAvailability ->
                if (!locationAvailability.isLocationAvailable) {
                    LoggerEdna.error("Location services are no longer available")
                }
            }

            LocationResult.extractResult(intent)?.let { locationResult ->
                locationResult.locations.map { location ->
                    LoggerEdna.debug("Location received.   $location")
                    try {
                        updateLocation(location)
                    } catch (exc: Exception) {
                        Handler(Looper.getMainLooper()).postDelayed({ updateLocation(location) }, 500)
                    }
                }
            }
        }
    }

    private fun updateLocation(location: Location) {
        BaseConfig.instance.transport.updateLocation(location.latitude, location.longitude)
    }

    companion object {
        const val ACTION_PROCESS_UPDATES =
            "com.google.android.gms.location.action.ACTION_PROCESS_UPDATES"
    }
}
