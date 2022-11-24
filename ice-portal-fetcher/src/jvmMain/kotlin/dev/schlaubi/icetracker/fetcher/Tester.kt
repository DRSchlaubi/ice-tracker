package dev.schlaubi.icetracker.fetcher

import dev.schlaubi.icetracker.client.ICEPortalClient
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.seconds

private val LOG = KotlinLogging.logger { }

public fun main() {
    val task = FetchingTask(
        interval = 2.seconds,
        client = ICEPortalClient(Url("http://localhost:80")),
        dispatcher = Dispatchers.IO
    )
    LOG.info { "Task started" }
    task.start()

    LOG.info { "Waiting for stdin to stop" }
    readLine()
    LOG.info { "Stopping and saving" }

    val prettyJson = Json {
        prettyPrint = true
    }

    val journey = runBlocking {
        prettyJson.encodeToString(task.stopAndSave())
    }
    val file = Files.createTempFile("journey", ".json")
    file.writeText(journey)
    LOG.info { "Tracking data saved to: ${file.absolutePathString()}" }
}