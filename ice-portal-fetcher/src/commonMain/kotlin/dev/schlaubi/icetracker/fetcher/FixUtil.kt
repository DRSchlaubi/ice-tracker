package dev.schlaubi.icetracker.fetcher

import kotlinx.datetime.Instant
import dev.schlaubi.icetracker.models.UnixTimestamp

/**
 * Fixes some issues with the previous [FetchingTask] implementation.
 * - Removes geo points that were also committed to previous segment
 * - Adds new segments for all gpsState changes
 */
public fun Journey.fixUp(): Journey {
    val fixedTest1Tracks = tracks.map { track ->
        val fixedSegments = buildList(track.segments.size) {
            var cutOff = emptyList<Journey.GeoSegment.Point>()
            repeat(track.segments.size) {
                val current = track.segments[it]
                // Fix geoPoints wrongly committed to this segment
                val currentPoints = current.points.filter { point -> point !in cutOff }
                cutOff = current.points

                // Fix wrongly not committed segments
                // As gpsState changes were committed to the old segment
                val allNewPoints = currentPoints.splitByChangeIn(Journey.GeoSegment.Point::gpsState)
                    .map { newPoints ->
                        current.copy(points = newPoints)
                    }

                addAll(allNewPoints)
            }
        }


        track.copy(segments = fixedSegments)
    }

    return copy(tracks = fixedTest1Tracks)
}

/**
 * Fixes other issues with the previous [UnixTimestamp] implementation.
 * - Converts milliseconds (interpreted as seconds) to valid milliseconds
 */
public fun Journey.fixUp2(): Journey {
    val fixedTracks = tracks.map { track ->
        val fixedSegments = track.segments.map { segment ->
            val fixedPoints = segment.points.map { point ->
                val milliseconds = point.timestamp.epochSeconds

                point.copy(timestamp = Instant.fromEpochMilliseconds(milliseconds))
            }

            segment.copy(points = fixedPoints)
        }
        track.copy(segments = fixedSegments)
    }
    return copy(tracks = fixedTracks)
}

private fun <T> List<T>.splitByChangeIn(selector: (T) -> Any) = buildList {
    val current = this@splitByChangeIn.firstOrNull()?.let { selector(it) }
    var size = 0
    fun next() {
        val nextBlock = this@splitByChangeIn.takeWhile { selector(it) == current }
        size += nextBlock.size
        add(nextBlock)
        if (size < this@splitByChangeIn.size) {
            next()
        }
    }

    next()
}