package dev.schlaubi.icetracker.fetcher

/**
 * Fixes some dead data at the end of each track.
 * - Removes segments with no change in distance
 * - Removes stations not tracked
 */
public fun Journey.cleanUp(): Journey = cleanUpDeadSegments().cleanUpStations()

private fun Journey.cleanUpDeadSegments(): Journey {
    val liveTracks =
        tracks.filter {
            it.segments.any { segment ->
                segment.points.any { point ->
                    point.speed > 5
                } && segment.points.size > 5
            }
        }

    return copy(tracks = liveTracks)
}

private fun Journey.cleanUpStations(): Journey {
    val firstStation = tracks.first().start
    val lastStation = tracks.last().end

    val remainingStations = stations.subList(indexOfEva(firstStation), indexOfEva(lastStation) + 1)

    return copy(stations = remainingStations)
}

private fun Journey.indexOfEva(eva: String) = stations.indexOfFirst { it.eva == eva }