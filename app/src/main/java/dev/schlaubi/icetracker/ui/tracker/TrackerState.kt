package dev.schlaubi.icetracker.ui.tracker

import android.os.Parcel
import android.os.Parcelable
import dev.schlaubi.icetracker.fetcher.Journey
import dev.schlaubi.icetracker.models.TrainStatus
import dev.schlaubi.icetracker.models.Trip
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TrackerState(
    val speed: String,
    val nextStop: String,
    val lastStop: String,
    val number: String,
    val train: Journey.TrainInfo
) : Parcelable {
    constructor(status: TrainStatus, trip: Trip) : this(
        status.speed.toString(),
        trip.stops.firstOrNull { it.station.eva == trip.stopInfo.actualNext }?.station?.name
            ?: trip.stopInfo.actualNext,
        trip.stops.firstOrNull { it.station.eva == trip.stopInfo.actualLast }?.station?.name
            ?: trip.stopInfo.actualLast,
        trip.number,
        Journey.TrainInfo(status.trainType, status.series, status.tzn)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(Json.encodeToString(this))
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TrackerState> {
        override fun createFromParcel(parcel: Parcel): TrackerState {
            return Json.decodeFromString(parcel.readString()!!)
        }

        override fun newArray(size: Int): Array<TrackerState?> {
            return arrayOfNulls(size)
        }
    }
}
