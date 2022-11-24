package dev.schlaubi.icetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.schlaubi.icetracker.fetcher.Journey
import dev.schlaubi.icetracker.ui.JourneyCard
import dev.schlaubi.icetracker.ui.theme.ICETrackerTheme
import kotlinx.datetime.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.setProperty("android.javax.xml.stream.XMLInputFactory", "com.sun.xml.stream.ZephyrParserFactory");
        System.setProperty("android.javax.xml.stream.XMLOutputFactory", "com.sun.xml.stream.ZephyrWriterFactory");
        System.setProperty("android.javax.xml.stream.XMLEventFactory", "com.sun.xml.stream.events.ZephyrEventFactory");
        setContent {
            ICETrackerTheme {
                App()
            }
        }
    }
}

data class DummyJourney(
    val name: String,
    val number: String,
    val trainInfo: Journey.TrainInfo,
    val date: Instant
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    Scaffold(topBar = {
        TopAppBar({ Text(text = stringResource(R.string.app_name)) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.surface,
                actionIconContentColor = MaterialTheme.colorScheme.surface,
            ),
            actions = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(imageVector = Icons.Filled.Add, stringResource(R.string.add_journey))
                }
            }
        )
    }, containerColor = MaterialTheme.colorScheme.primaryContainer) { padding ->
        Row(Modifier.padding(padding)) {
            Column(
                Modifier
                    .padding(vertical = 15.dp, horizontal = 5.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                defaultJourneys().forEach { journey ->
                    JourneyCard(journey)
                }
            }
        }
    }
}
