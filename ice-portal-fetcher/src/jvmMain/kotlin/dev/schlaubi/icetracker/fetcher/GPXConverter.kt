package dev.schlaubi.icetracker.fetcher

import dev.schlaubi.icetracker.models.Trip
import io.jenetics.jpx.*
import kotlinx.datetime.toJavaInstant
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Text
import javax.xml.parsers.DocumentBuilderFactory

private val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()

public fun Journey.toGPX(includeExtensions: Boolean = true): GPX {
    return GPX.builder().apply {
        stations.forEach { station ->
            addWayPoint { wayPoint ->
                wayPoint
                    .lat(station.geoCoordinates.latitude)
                    .lon(station.geoCoordinates.longitude)
                    .speed(0.0)
                    .name(station.name)
            }
        }

        metadata { meta ->
            meta.author(
                Person.of(
                    "ICETracker",
                    null,
                    Link.of("https://github.com/DRSchlaubi/icetracker")
                )
            )
        }
        if (includeExtensions) {
            extensions(this@toGPX.extensions())
        }
        val stationByEva = stations.associateBy(Trip.Station::eva)
        addTrack { track ->
            track.name(name)
            tracks.forEach { trackData ->
                addRoute { route ->
                    route.name("${stationByEva[trackData.start]?.name} - ${stationByEva[trackData.end]?.name}")
                    if (includeExtensions) {
                        route.extensions(trackData.extensions())
                    }
                    trackData.segments.forEach { segmentData ->
                        track.addSegment { segment ->
                            segmentData.points.forEach { pointData ->
                                val point = WayPoint.builder()
                                    .lat(pointData.latitude)
                                    .lon(pointData.longitude)
                                    .speed(
                                        Speed.of(
                                            pointData.speed.toDouble(),
                                            Speed.Unit.KILOMETERS_PER_HOUR
                                        )
                                    )
                                    .time(pointData.timestamp.toJavaInstant())
                                    .apply {
                                        if (includeExtensions) {
                                            extensions(pointData.extensions())
                                        }
                                    }
                                    .build()

                                route.addPoint(point)
                                segment.addPoint(point)
                            }
                        }
                    }
                }
            }
        }
    }.build()
}


private fun newDocument() = documentBuilder.newDocument()

private fun Journey.GeoSegment.Point.extensions(): Document = newExtensions {
    val iceExtension = createElement("ICEPointExtension").apply {
        val wifiState = createElement("wifiState").apply {
            appendChild(createTextNode(wifiState))
        }
        val gpsState = createElement("gpsState").apply {
            appendChild(createTextNode(gpsState))
        }

        appendChild(wifiState)
        appendChild(gpsState)
    }
    appendChild(iceExtension)
}

private fun Journey.GeoTrack.extensions(): Document = newExtensions {
    val iceExtension = createElement("ICETrackEExtension").apply {
        val start = createElement("start").apply {
            appendChild(createTextNode(start))
        }
        val end = createElement("end").apply {
            appendChild(createTextNode(end))
        }

        appendChild(start)
        appendChild(end)
    }
    appendChild(iceExtension)
}

private fun Journey.extensions(): Document = newExtensions {
    val iceExtension = createElement("ICEJourneyExtension").apply {
        val number = createElement("trainNumber").apply {
            appendChild(createTextNode(number))
        }
        val trainType = createElement("trainType").apply {
            appendChild(createTextNode(trainInfo.type))
        }
        val series = createElement("buildingSeries").apply {
            appendChild(createTextNode(trainInfo.buildingSeries))
        }
        val tzn = createElement("tzn").apply {
            appendChild(createTextNode(trainInfo.tzn))
        }

        appendChild(number)
        appendChild(trainType)
        appendChild(series)
        appendChild(tzn)
    }
    appendChild(iceExtension)
}

private inline fun newExtensions(block: ExtensionsBuilder.() -> Unit) = newDocument().apply {
    appendChild(ExtensionsBuilder(createElement("extensions"), this).apply(block).element)
}

private class ExtensionsBuilder(val element: Element, val root: Document) : Element by element {
    fun createElement(name: String): Element = root.createElement(name)
    fun createTextNode(name: String): Text = root.createTextNode(name)
}
