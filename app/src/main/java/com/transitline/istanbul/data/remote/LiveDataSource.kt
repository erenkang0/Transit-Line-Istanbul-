package com.transitline.istanbul.data.remote

/** One live arrival as a real-time source would report it. */
data class LiveArrival(
    val lineId: String,
    val etaMinutes: Int,
)

/**
 * Seam for real-time İETT data. The offline app NEVER depends on this being
 * reachable: when [arrivalsAtStop] returns null the repository transparently
 * falls back to the bundled schedule and shows a polite "showing local data"
 * note instead of an error.
 *
 * A concrete implementation (İBB SOAP `SeferGerceklesme` / GTFS-Realtime) is
 * intentionally deferred until the data-source choice is approved. See
 * docs/API_RESEARCH.md for the evaluation.
 */
interface LiveDataSource {
    val isConnected: Boolean

    /** Returns null when no live data is available for [stopCode]. */
    suspend fun arrivalsAtStop(stopCode: String): List<LiveArrival>?
}

/** Default wiring: no live source. Everything is served from the local schedule. */
class OfflineOnlyLiveDataSource : LiveDataSource {
    override val isConnected: Boolean = false
    override suspend fun arrivalsAtStop(stopCode: String): List<LiveArrival>? = null
}
