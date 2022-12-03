package dev.schlaubi.icetracker.fetcher

import dev.schlaubi.icetracker.models.Trip
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Suppress("DataClassPrivateConstructor") // only used for serialization
@Serializable
public data class Journey private constructor(
    val version: Int = 1,
    val name: String,
    val id: String,
    val number: String,
    val trainInfo: TrainInfo,
    val stations: List<Trip.Station>,
    val createdAt: Instant,
    val tracks: List<GeoTrack>
) {
    public constructor(
        name: String,
        id: String,
        number: String,
        trainInfo: TrainInfo,
        stations: List<Trip.Station>,
        createdAt: Instant,
        tracks: List<GeoTrack>
    ) : this(CURRENT_VERSION, name, id, number, trainInfo, stations, createdAt, tracks)

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

    public companion object {
        public const val CURRENT_VERSION: Int = 4
    }
}