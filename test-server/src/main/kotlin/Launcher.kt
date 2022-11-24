package dev.schlaubi.icetracker.test_server

import dev.schlaubi.icetracker.models.TrainStatus
import dev.schlaubi.icetracker.models.Trip
import dev.schlaubi.icetracker.models.TripInfo
import dev.schlaubi.icetracker.routes.ICEPortal
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText
import kotlin.random.Random

@Serializable
private data class Vehicle(
    val maxSpeed: Int,
    val tzn: String,
    val type: String,
    val series: String,
    val trainType: String
)

@Serializable
private data class Journey(
    val number: Int,
    val geoPoints: List<GeoPoint>,
    val stops: List<Trip.Stop>
)

@Serializable
private data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val stopInfo: Trip.StopInfo
)

fun main() {
    val startTime = Clock.System.now()

    val testData = Path("test-data")
    val journeyFile = (testData / "journeys").listDirectoryEntries("*.json").first()
    val vehicleFile = (testData / "vehicles").listDirectoryEntries("*.json").first()
    val journey = Json.decodeFromString<Journey>(journeyFile.readText())
    val vehicle = Json.decodeFromString<Vehicle>(vehicleFile.readText())
    val date = java.time.LocalDate.now().toKotlinLocalDate()

    fun geoPoint(): GeoPoint {
        val now = Clock.System.now()
        val timeDiff = (now - startTime).inWholeSeconds / 4
        println("Selected point: $timeDiff")
        return journey.geoPoints[timeDiff.toInt()]
    }

    embeddedServer(Netty) {
        install(ContentNegotiation) {
            json()
        }
        install(Resources)

        routing {
            get<ICEPortal.API1.Trip> {
                val geoPoint = geoPoint()

                context.respond(
                    TripInfo(
                        Trip(
                            date,
                            vehicle.trainType,
                            journey.number.toString(),
                            -1,
                            -1,
                            100000,
                            geoPoint.stopInfo,
                            journey.stops
                        ),
                        JsonObject(emptyMap()),
                        null
                    )
                )
            }
            get<ICEPortal.API1.Status> {
                val (lat, lon) = geoPoint()
                call.respond(
                    TrainStatus(
                        true,
                        "HIGH",
                        "VALID",
                        "HIGH",
                        lat, lon,
                        0, 0,
                        vehicle.series,
                        Clock.System.now(),
                        Random.nextInt(vehicle.maxSpeed),
                        vehicle.trainType,
                        vehicle.tzn,
                        TrainStatus.WagonClass.FIRST,
                        TrainStatus.Connectivity("", "", 10000),
                        false
                    )
                )
            }
        }
    }.start(wait = true)
}
