package dev.schlaubi.icetracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import dev.schlaubi.icetracker.service.TrackerService
import dev.schlaubi.icetracker.service.TrackingServiceState
import dev.schlaubi.icetracker.ui.journey.JourneyList
import dev.schlaubi.icetracker.ui.theme.ICETrackerTheme
import dev.schlaubi.icetracker.ui.tracker.TrackerActivity
import kotlinx.datetime.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ICETrackerTheme {
                App()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val context = LocalContext.current
    val trackerState by TrackerService.state.observeAsState()
    Scaffold(topBar = {
        TopAppBar({ Text(text = stringResource(R.string.app_name)) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.surface,
                actionIconContentColor = MaterialTheme.colorScheme.surface,
            ),
            actions = {
                IconButton(onClick = {
                    if (trackerState is TrackingServiceState.Stopping) {
                        TrackerService.reset()
                    }
                    context.startActivity(
                        Intent(
                            context,
                            TrackerActivity::class.java
                        )
                    )
                }) {
                    val (icon, description) = if (trackerState is TrackingServiceState.Running) {
                        Icons.Filled.Info to stringResource(R.string.tracker_settings)
                    } else {
                        Icons.Filled.Add to stringResource(R.string.add_journey)
                    }
                    Icon(icon, description)
                }
                ActionDropDown()
            }
        )
    }, containerColor = MaterialTheme.colorScheme.primaryContainer) { padding ->
        Row(Modifier.padding(padding)) {
            JourneyList()
        }
    }
}

@Composable
private fun ActionDropDown() {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    IconButton(onClick = { expanded = true }) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(R.string.options)
        )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(text = { Text(stringResource(R.string.import_item)) }, onClick = {
            expanded = false
        })

        DropdownMenuItem(text = { Text(stringResource(R.string.open_source_licenses)) }, onClick = {
            expanded = false

            context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
        })
    }
}
