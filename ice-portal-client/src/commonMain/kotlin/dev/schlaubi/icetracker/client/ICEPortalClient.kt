package dev.schlaubi.icetracker.client

import dev.schlaubi.icetracker.models.TrainStatus
import dev.schlaubi.icetracker.models.TripInfo
import dev.schlaubi.icetracker.routes.ICEPortal
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

public class ICEPortalClient(
    public val url: Url = Url("https://iceportal.de"),
    client: HttpClient = HttpClient()
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val client = client.config {
        install(ContentNegotiation) {
            json(json)
        }
        expectSuccess = true
        install(Resources)
        defaultRequest {
            url {
                takeFrom(this@ICEPortalClient.url)
                appendPathSegments(url.pathSegments.filterNot(String::isBlank))
            }
        }
    }

    /**
     * Retrieves the current [TrainStatus].
     */
    public suspend fun getTrainStatus(): TrainStatus =
        client.get(ICEPortal.API1.Status()).body()

    /**
     * Retrieves the current [TripInfo].
     */
    public suspend fun getTripInfo(): TripInfo =
        client.get(ICEPortal.API1.Trip()).body()
}
