package dev.schlaubi.icetracker.ui.tracker

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.schlaubi.icetracker.R
import dev.schlaubi.icetracker.ui.journey.JourneyDetail


@Composable
fun TrackingTrackerContent(
    state: TrackerState,
    paused: Boolean,
    waitingForServiceUpdate: Boolean,
    onPause: () -> Unit,
    onSave: () -> Unit,
    onResume: () -> Unit
) {
    Column(Modifier.padding(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        CurrentVelocity(state.speed)
        TrackerDivider()
        StationInfo(state.nextStop, state.lastStop)
        TrackerDivider()
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            JourneyDetail(
                Icons.Filled.Train,
                R.string.building_series,
                state.train.buildingSeries
            )
            JourneyDetail(
                Icons.Filled.Train,
                R.string.train_line,
                state.number
            )
            JourneyDetail(
                Icons.Filled.Tram,
                R.string.tzn,
                state.train.tzn
            )
        }

        if (paused) {
            RowButton(onClick = onResume, enabled = !waitingForServiceUpdate) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = stringResource(R.string.resume)
                )
                Text(stringResource(R.string.resume))
            }
        } else {
            RowButton(onClick = onPause, enabled = !waitingForServiceUpdate) {
                Icon(
                    imageVector = Icons.Filled.Pause,
                    contentDescription = stringResource(R.string.pause)
                )
                Text(stringResource(R.string.pause))
            }
        }
        RowButton(onClick = onSave, enabled = !waitingForServiceUpdate) {
            Icon(
                imageVector = Icons.Filled.Save,
                contentDescription = stringResource(R.string.stop_and_save)
            )
            Text(stringResource(R.string.stop_and_save))
        }
    }
}

@Composable
private fun TrackerDivider() {
    Divider(
        Modifier
            .fillMaxWidth(.9f)
            .padding(vertical = 10.dp)
    )
}

@Composable
private fun StationInfo(nextStop: String, lastStop: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Station(nextStop, stringResource(R.string.next_stop))
        Divider(
            Modifier
                .fillMaxHeight()
                .width(1.dp)
        )
        Station(lastStop, stringResource(R.string.last_stop))
    }
}

@Composable
private fun CurrentVelocity(speed: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(text = speed, style = MaterialTheme.typography.displayLarge)
        Text(
            text = stringResource(R.string.current_velocity),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
