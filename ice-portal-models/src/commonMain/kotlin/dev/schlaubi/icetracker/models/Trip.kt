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
    val tripDate: LocalDate,
    val trainType: String,
    @SerialName("vzn") val number: String,
    val actualPosition: Int,
    val distanceFromLastStop: Int,
    val totalDistance: Int
)
