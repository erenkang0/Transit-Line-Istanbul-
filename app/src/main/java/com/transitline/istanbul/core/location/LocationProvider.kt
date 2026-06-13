package com.transitline.istanbul.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Thin wrapper over the platform [LocationManager]. Deliberately avoids Google
 * Play Services so the app stays lean and works on any device. Location is only
 * ever used to highlight the nearest metro station and is gated by Power Saving.
 */
class LocationProvider(private val context: Context) {

    private val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun hasPermission(): Boolean {
        val ctx = context
        val fine = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun lastKnown(): Location? {
        if (!hasPermission()) return null
        var best: Location? = null
        for (provider in lm.getProviders(true)) {
            val candidate = lm.getLastKnownLocation(provider) ?: continue
            if (best == null || candidate.accuracy < best.accuracy) best = candidate
        }
        return best
    }

    @SuppressLint("MissingPermission")
    suspend fun requestSingle(): Location? {
        if (!hasPermission()) return null
        val provider = when {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> return lastKnown()
        }
        return suspendCancellableCoroutine { cont ->
            val listener = LocationListener { location ->
                if (cont.isActive) cont.resume(location)
            }
            lm.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            cont.invokeOnCancellation { lm.removeUpdates(listener) }
        }
    }
}
