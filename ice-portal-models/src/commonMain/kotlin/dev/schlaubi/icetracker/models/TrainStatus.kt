package dev.schlaubi.icetracker.models

import kotlinx.serialization.Serializable

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
    val serverTime: UnixTimestamp,
    val speed: Int,
    val trainType: String,
    val tzn: String,
    val wagonClass: WagonClass,
    val connectivity: Connectivity,
    val bapInstalled: Boolean
) {
    @Serializable
    public data class Connectivity(
        val currentState: String,
        val nextState: String,
        val remainingSeconds: Int
    )

    @Serializable
    public enum class WagonClass {
        FIRST,
        SECOND
    }
}