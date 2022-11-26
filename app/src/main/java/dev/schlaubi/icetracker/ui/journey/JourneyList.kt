package dev.schlaubi.icetracker.ui.journey

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.schlaubi.icetracker.R
import dev.schlaubi.icetracker.util.defaultJourneys
import dev.schlaubi.icetracker.fetcher.Journey
import dev.schlaubi.icetracker.service.TrackerService
import dev.schlaubi.icetracker.service.TrackingServiceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.nio.file.Path
import kotlin.io.path.*

private data class SavedJourney(
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
                journeys = files.mapNotNull {
                    runCatching {
                        val journey = Json.decodeFromStream<Journey>(it.inputStream())

                        SavedJourney(journey, it)
                    }.getOrNull()
                }
                loading = false
            }
            onDispose { }
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

@Composable
private fun AddDefaultJourneyButton(addJourney: (journey: Journey) -> Unit) {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { addJourney(defaultJourneys(1).first()) }) {
            Text(text = stringResource(id = R.string.add_default_journey))
        }
    }
}
