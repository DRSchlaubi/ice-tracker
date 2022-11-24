package dev.schlaubi.icetracker.fetcher

import dev.schlaubi.icetracker.models.Trip
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
public data class Journey(
    val name: String,
    val number: String,
    val trainInfo: TrainInfo,
    val stations: List<Trip.Station>,
    val createdAt: Instant,
    val tracks: List<GeoTrack>,
) {
    @Serializable
    public data class TrainInfo(
        val type: String,
        val buildingSeries: String,
        val tzn: String
    )

    @Serializable
    public data class GeoTrack(
        val start: String,
        val end: String,
        val segments: List<GeoSegment>
    )

    @Serializable
    public data class GeoSegment(
        val points: List<Point>,
        val error: String? = null
    ) {
        @Serializable
        public data class Point(
            val latitude: Double,
            val longitude: Double,
            val timestamp: Instant,
            val speed: Int,
            val wifiState: String,
            val gpsState: String
        )
    }
}