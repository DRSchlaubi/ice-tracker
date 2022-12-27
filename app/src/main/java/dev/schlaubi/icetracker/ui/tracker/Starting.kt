package dev.schlaubi.icetracker.ui.tracker

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.schlaubi.icetracker.R
import dev.schlaubi.icetracker.client.ICEPortalClient
import dev.schlaubi.icetracker.service.TRACKER_IGNORE_SSL_EXTRA
import dev.schlaubi.icetracker.service.TRACKER_INITIAL_DATA_EXTRA
import dev.schlaubi.icetracker.service.TrackerService
import dev.schlaubi.icetracker.util.icePortalClient
import dev.schlaubi.icetracker.util.sslIgnoringIcePortalClient
import io.ktor.client.plugins.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun StartingTracker(activity: Activity) {
    var error by remember { mutableStateOf<String?>(null) }
    var showSslWarning by remember { mutableStateOf(false) }
    var tryWithoutSsl by remember { mutableStateOf(false) }

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

    if (showSslWarning) {
        AlertDialog(
            { showSslWarning = false },
            containerColor = MaterialTheme.colorScheme.errorContainer,
            icon = {
                Icon(
                    Icons.Filled.Warning,
                    stringResource(R.string.invalid_ssl_warning_title)
                )
            },
            title = { Text(stringResource(R.string.invalid_ssl_warning_title)) },
            text = { Text(stringResource(R.string.invalid_ssl_warning)) },
            dismissButton = {
                Button(onClick = { showSslWarning = false; activity.finish() }) {
                    Text(stringResource(R.string.abort))
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSslWarning = false; tryWithoutSsl = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.continue_anyways))
                }
            }
        )
    }


    @Composable
    fun TryStart(
        client: ICEPortalClient,
        canRecover: Boolean = true,
        ignoreSslInTask: Boolean = false
    ) {
        val coroutineScope = rememberCoroutineScope()

        suspend fun <T> catch(block: suspend () -> T): T? {
            return try {
                block()
            } catch (e: ResponseException) {
                error = "Unexpected response code: ${e.response.status}"
                null
            } catch (e: Throwable) {
                Log.e("ICTRK", "Could not query portal", e)
                error = e.message
                showSslWarning = e.message == "Chain validation failed" && canRecover
                null
            }
        }

        DisposableEffect(Unit) {
            error = null
            coroutineScope.launch(Dispatchers.IO) {
                val trip = catch { client.getTripInfo() } ?: return@launch
                val status = catch { client.getTrainStatus() } ?: return@launch

                val initialData = TrackerState(status, trip.trip)

                val intent =
                    TrackerService.commandIntent(activity, TrackerService.Command.START) {
                        putExtra(TRACKER_INITIAL_DATA_EXTRA, initialData)
                        putExtra(TRACKER_IGNORE_SSL_EXTRA, ignoreSslInTask)
                    }
                activity.startForegroundService(intent)
            }
            onDispose { }
        }
    }

    if (tryWithoutSsl) {
        TryStart(sslIgnoringIcePortalClient, canRecover = false, ignoreSslInTask = true)
    } else {
        TryStart(icePortalClient)
    }
}
