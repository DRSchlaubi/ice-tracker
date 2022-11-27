package dev.schlaubi.icetracker.fetcher

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