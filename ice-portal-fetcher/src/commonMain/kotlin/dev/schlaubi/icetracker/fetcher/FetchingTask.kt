package dev.schlaubi.icetracker.fetcher

import dev.schlaubi.icetracker.client.ICEPortalClient
import dev.schlaubi.icetracker.models.TrainStatus
import dev.schlaubi.icetracker.models.Trip
import dev.schlaubi.icetracker.models.TripInfo
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import mu.KotlinLogging
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val LOG = KotlinLogging.logger { }

public class FetchingTask(
    private val interval: Duration = 10.seconds,
    private val client: ICEPortalClient = ICEPortalClient(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = dispatcher + Job()

    private val createdAt = Clock.System.now()
    private var lastTripInfo: TripInfo? = null
    private var lastStatus: TrainStatus? = null
    private var currentGeoPoints = mutableListOf<Journey.GeoSegment.Point>()
    private var currentSegments = mutableListOf<Journey.GeoSegment>()
    private var tracks = mutableListOf<Journey.GeoTrack>()

    private lateinit var job: Job

    public fun start() {
        if (::job.isInitialized) error("Already running!")
        job = launch {
            run()
        }
    }

    private suspend fun run() {
        if (isActive) {
            try {
                fetchCurrentStatus()
            } catch (e: Exception) {
                commitSegment(e)
                LOG.error(e) { "Could not process query" }
            }
            LOG.trace { "Next fetch in $interval" }
            delay(interval)
            run()
        }
    }

    public fun stopAndSave(): Journey {
        job.cancel()
        commitTrack()
        val latestTripInfo = lastTripInfo ?: error("No data available")
        val latestStatus = lastStatus ?: error("No data available")

        return Journey(
            latestTripInfo.trip.number,
            latestTripInfo.trip.number,
            generateNonce(),
            Journey.TrainInfo(
                latestStatus.trainType,
                latestStatus.series,
                latestStatus.tzn
            ),
            latestTripInfo.trip.stops.map(Trip.Stop::station),
            createdAt,
            tracks
        )
    }

    private suspend fun fetchCurrentStatus() {
        val currentStatus = client.getTrainStatus()
        LOG.trace { "Fetched train status: $currentStatus" }
        val currentTripInfo = client.getTripInfo()
        LOG.trace { "Fetched trip info: $currentTripInfo" }

        if (lastTripInfo?.trip?.stopInfo?.actualNext != currentTripInfo.trip.stopInfo.actualNext) {
            LOG.info { "Station changed! adding new track" }
            commitTrack()
        } else if (currentStatus.gpsStatus != lastStatus?.gpsStatus) {
            LOG.info { "GPS state changed adding new segment" }
            if (currentGeoPoints.isEmpty()) {
                LOG.debug { "Geo points are empty, skipping segment" }
            } else {
                currentSegments.add(Journey.GeoSegment(currentGeoPoints.toList()))
            }
        }
        lastTripInfo = currentTripInfo
        lastStatus = currentStatus
        val nextGeoPoint = Journey.GeoSegment.Point(
            currentStatus.latitude,
            currentStatus.longitude,
            currentStatus.serverTime,
            currentStatus.speed,
            currentStatus.internet,
            currentStatus.gpsStatus
        )
        LOG.debug { "Adding GeoPoint: $nextGeoPoint" }
        currentGeoPoints += nextGeoPoint
    }

    private fun commitSegment(e: Throwable? = null) {
        if (currentGeoPoints.isEmpty()) {
            LOG.debug { "There were no geo-points saved. Skipping segment (likely initial segment)" }
        } else {
            currentSegments.add(
                Journey.GeoSegment(
                    currentGeoPoints.toList(),
                    e?.stackTraceToString()
                )
            )
        }
        currentGeoPoints.clear()
    }

    private fun commitTrack() {
        commitSegment() // Commit last segment
        if (currentSegments.isEmpty()) {
            LOG.debug { "No segments! Skipping track" }
        } else {
            tracks.add(
                Journey.GeoTrack(
                    lastTripInfo!!.trip.stopInfo.actualLast,
                    lastTripInfo!!.trip.stopInfo.actualNext,
                    currentSegments.toList()
                )
            )
            currentSegments.clear()
            currentGeoPoints.clear()
        }
    }
}
