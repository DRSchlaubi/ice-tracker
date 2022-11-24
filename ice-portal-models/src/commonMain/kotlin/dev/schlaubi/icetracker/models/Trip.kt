package dev.schlaubi.icetracker.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
public data class TripInfo(
    val trip: Trip,
    // We don't have clear data on this
    val connection: JsonObject,
    val active: JsonElement? = null
)

@Serializable
public data class Trip(
    val tripDate: DBLocalDate,
    val trainType: String,
    @SerialName("vzn") val number: String,
    val actualPosition: Int,
    val distanceFromLastStop: Int,
    val totalDistance: Int,
    val stopInfo: StopInfo,
    val stops: List<Stop>
) {
    @Serializable
    public data class StopInfo(
        val scheduledNext: String,
        val actualNext: String,
        val actualLast: String
    )

    @Serializable
    public data class Stop(val station: Station)

    @Serializable
    public data class Station(
        @SerialName("evaNr") val eva: String,
        val name: String,
        @SerialName("geocoordinates") val geoCoordinates: GeoCoordinates
    )
}

@Serializable
public data class GeoCoordinates(val latitude: Double, val longitude: Double)
