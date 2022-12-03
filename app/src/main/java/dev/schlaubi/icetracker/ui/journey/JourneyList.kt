package dev.schlaubi.icetracker.ui.journey

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.schlaubi.icetracker.R
import dev.schlaubi.icetracker.fetcher.Journey
import dev.schlaubi.icetracker.fetcher.cleanUp
import dev.schlaubi.icetracker.fetcher.fixUp
import dev.schlaubi.icetracker.fetcher.fixUp2
import dev.schlaubi.icetracker.service.TrackerService
import dev.schlaubi.icetracker.service.TrackingServiceState
import dev.schlaubi.icetracker.util.defaultJourneys
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.nio.file.Path
import kotlin.io.path.*

data class SavedJourney(
    val journey: Journey,
    val file: Path
)


@OptIn(ExperimentalSerializationApi::class)
@Composable
fun JourneyList() {
    val journeyDirectory = LocalContext.current.filesDir.toPath() / "journeys"
    val coroutineScope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var journeys by remember { mutableStateOf(emptyList<SavedJourney>()) }
    val trackerState by TrackerService.state.observeAsState()

    fun removeJourney(id: String) {
        journeys = journeys.filter { it.journey.id != id }
    }

    fun addJourney(journey: Journey) {
        if (journeys.any { it.journey.id == journey.id }) return
        coroutineScope.launch(Dispatchers.IO) {
            val file = journeyDirectory / "journey_${journey.id}.journey.json"
            Json.encodeToStream(journey, file.outputStream())
            journeys = journeys + SavedJourney(journey, file)
        }
    }


    val currentTrackerState = trackerState
    if (currentTrackerState is TrackingServiceState.Stopping) {
        addJourney(currentTrackerState.data)
    }

    if (loading) {
        DisposableEffect(Unit) {
            coroutineScope.launch(Dispatchers.IO) {
                if (!journeyDirectory.exists()) {
                    journeyDirectory.createDirectories()
                }
                val files = journeyDirectory.listDirectoryEntries("*.journey.json")
                journeys = files.flatMap {
                    runCatching {
                        it.fixJourneyIfNeeded()
                    }.getOrNull() ?: emptyList()
                }
                loading = false
            }
            onDispose { }
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(7.dp)
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            Modifier
                .padding(vertical = 15.dp, horizontal = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            journeys.forEach { (journey, file) ->
                JourneyCard(journey, file, ::removeJourney)
            }

            if (journeys.isEmpty()) {
                AddDefaultJourneyButton(::addJourney)
            }
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
private fun Path.fixJourneyIfNeeded(): List<SavedJourney> {
    val roundOne = Json.decodeFromStream<Journey>(inputStream())
    fun fix(newVersion: Int, fixer: Journey.() -> Journey): List<SavedJourney> {
        val backupId = "${roundOne.id}-${generateNonce()}"
        val backup =
            roundOne.copy(name = "${roundOne.name} - Backup", version = newVersion, id = backupId)
        val fixed = roundOne.fixer().copy(version = newVersion)
        writeText(Json.encodeToString(fixed))
        val backupFile = parent / "journey_$backupId.bak.journey.json"
        backupFile.writeText(Json.encodeToString(backup))

        return listOf(
            SavedJourney(backup, backupFile),
            SavedJourney(fixed, this@fixJourneyIfNeeded)
        )
    }

    return when (roundOne.version) {
        1 -> fix(Journey.CURRENT_VERSION) {
            fixUp().fixUp2().cleanUp()
        }
        2 -> fix(Journey.CURRENT_VERSION) {
            fixUp2().cleanUp()
        }
        3 -> fix(Journey.CURRENT_VERSION, Journey::cleanUp)
        else -> listOf(SavedJourney(roundOne, this))
    }
}


@Composable
private fun AddDefaultJourneyButton(addJourney: (journey: Journey) -> Unit) {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { addJourney(defaultJourneys(1).first()) }) {
            Text(text = stringResource(id = R.string.add_default_journey))
        }
    }
}
