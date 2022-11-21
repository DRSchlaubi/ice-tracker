package dev.schlaubi.icetracker.test_server

import dev.schlaubi.icetracker.models.TrainStatus
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
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
private data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

fun main() {
    val startTime = Clock.System.now()

    val testData = Path("test-data")
    val journeyFile = (testData / "journeys").listDirectoryEntries("*.json").first()
    val vehicleFile = (testData / "vehicles").listDirectoryEntries("*.json").first()
    val journey = Json.decodeFromString<List<GeoPoint>>(journeyFile.readText())
    val vehicle = Json.decodeFromString<Vehicle>(vehicleFile.readText())

    embeddedServer(Netty) {
        install(ContentNegotiation) {
            json()
        }
        install(Resources)

        routing {
            get<ICEPortal.API1.Status> {
                val now = Clock.System.now()
                val timeDiff = (now - startTime).inWholeSeconds % journey.size
                val (lat, lon) = journey[timeDiff.toInt()]

                call.respond(
                    TrainStatus(
                        true,
                        "HIGH",
                        "VALID",
                        "",
                        lat, lon,
                        0, 0,
                        vehicle.series,
                        now,
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
