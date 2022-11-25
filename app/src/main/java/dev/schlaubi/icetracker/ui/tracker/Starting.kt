package dev.schlaubi.icetracker.ui.tracker

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.schlaubi.icetracker.R
import dev.schlaubi.icetracker.service.TRACKER_INITIAL_DATA_EXTRA
import dev.schlaubi.icetracker.service.TrackerService
import dev.schlaubi.icetracker.util.icePortalClient
import io.ktor.client.plugins.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun StartingTracker(activity: Activity) {
    val coroutineScope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }

    suspend fun <T> catch(block: suspend () -> T): T? {
        return try {
            block()
        } catch (e: ServerResponseException) {
            error = "Unexpected response code: ${e.response.status}"
            null
        } catch (e: Throwable) {
            Log.e("ICTRK", "Could not query portal", e)
            error = e.message
            null
        }
    }

    DisposableEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val trip = catch { icePortalClient.getTripInfo() } ?: return@launch
            val status = catch { icePortalClient.getTrainStatus() } ?: return@launch

            val initialData = TrackerState(status, trip.trip)

            val intent =
                TrackerService.commandIntent(activity, TrackerService.Command.START) {
                    putExtra(TRACKER_INITIAL_DATA_EXTRA, initialData)
                }
            activity.startForegroundService(intent)
        }
        onDispose { }
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp, horizontal = 5.dp)
    ) {
        if (error != null) {
            Text(
                text = stringResource(R.string.could_not_connect_to_fis, error.toString()),
                textAlign = TextAlign.Center
            )
        } else {
            CircularProgressIndicator()
        }
    }
}