package dev.schlaubi.icetracker.models

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
public data class TrainStatus(
    val connection: Boolean,
    val serviceLevel: String,
    val gpsStatus: String,
    val internet: String,
    val latitude: Double,
    val longitude: Double,
    val tileY: Int,
    val tileX: Int,
    val series: String,
    @Serializable(with = UnixTimestampSerializer::class)
    val serverTime: Instant,
    val speed: Double,
    val trainType: String,
    val tzn: String,
    val wagonClass: WagonClass =WagonClass.FIRST,
    val connectivity: Connectivity? = null,
    val bapInstalled: Boolean
) {
    @Serializable
    public data class Connectivity(
        val currentState: String? = null,
        val nextState: String? = null,
        val remainingSeconds: Int? = null
    )

    @Serializable
    public enum class WagonClass {
        FIRST,
        SECOND
    }
}