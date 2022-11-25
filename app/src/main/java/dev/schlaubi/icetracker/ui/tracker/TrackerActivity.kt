package dev.schlaubi.icetracker.ui.tracker

import android.app.Activity
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
import androidx.compose.ui.res.stringResource
import dev.schlaubi.icetracker.R
import dev.schlaubi.icetracker.service.TrackerService
import dev.schlaubi.icetracker.service.TrackingServiceState
import dev.schlaubi.icetracker.ui.theme.ICETrackerTheme

const val STARTED_FROM_MAIN_SCREEN = "STARTED_FROM_MAIN_SCREEN"

class TrackerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ICETrackerTheme {
                TrackerView(this, intent.getBooleanExtra(STARTED_FROM_MAIN_SCREEN, true))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackerView(
    activity: Activity,
    startedFromMainScreen: Boolean
) {
    var paused by remember { mutableStateOf(false) }
    val state by TrackerService.state.observeAsState(initial = TrackerService.state.value!!)

    Scaffold(
        topBar = {
            TopAppBar(
                { Text(text = stringResource(R.string.tracker_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.surface,
                    actionIconContentColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                },
                navigationIcon = {
                    IconButton(onClick = { activity.finish() }) {
                        val (icon, description) = if (startedFromMainScreen) {
                            Icons.Filled.ArrowBack to stringResource(id = R.string.back)
                        } else {
                            Icons.Filled.Close to stringResource(id = R.string.close)
                        }
                        Icon(imageVector = icon, contentDescription = description)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) { padding ->
        Row(Modifier.padding(padding)) {
            val currentState = state
            if (currentState is TrackingServiceState.Sleeping) {
                StartingTracker(activity)
            } else if (currentState is TrackingServiceState.Running) {
                TrackingTrackerContent(
                    state = currentState.data,
                    paused = paused,
                    onPause = {
                        paused = true
                        activity.startForegroundService(
                            TrackerService.commandIntent(
                                activity,
                                TrackerService.Command.PAUSE
                            )
                        )
                    },
                    onSave = {
                        activity.startForegroundService(
                            TrackerService.commandIntent(
                                activity,
                                TrackerService.Command.STOP
                            )
                        )
                        activity.finishActivity(1)
                    },
                    onResume = {
                        paused = false
                        activity.startForegroundService(
                            TrackerService.commandIntent(
                                activity,
                                TrackerService.Command.RESUME
                            )
                        )
                    },
                    waitingForServiceUpdate = paused != (state as? TrackingServiceState.Running)?.paused
                )
            } else if (currentState is TrackingServiceState.Stopping) {
                activity.finish()
            }
        }
    }
}


