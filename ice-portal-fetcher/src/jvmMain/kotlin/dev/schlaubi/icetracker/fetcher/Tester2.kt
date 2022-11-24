package dev.schlaubi.icetracker.fetcher

import io.jenetics.jpx.GPX
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText

private val LOG = KotlinLogging.logger { }

public fun main() {
    val file = Path("test-data/saved-journeys").listDirectoryEntries("*.json").first()
    val parsed = Json.decodeFromString<Journey>(file.readText())
    val gpx = parsed.toGPX()
    val output = Files.createTempFile("journey_", ".gpx")
    GPX.write(gpx, output)
    LOG.info { "Saved to: ${output.absolutePathString()}" }
}