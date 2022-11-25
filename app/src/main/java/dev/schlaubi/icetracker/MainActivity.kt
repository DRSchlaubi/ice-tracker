package dev.schlaubi.icetracker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
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
    var notificationPermissionDialogPresent by remember { mutableStateOf(false) }
    val permissionRequester =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                context.startTracker()
            } else {
                notificationPermissionDialogPresent = true
            }
        }

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

                    context.requestNotificationPermission(permissionRequester)
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
    if (notificationPermissionDialogPresent) {
        AlertDialog(
            onDismissRequest = { notificationPermissionDialogPresent = false },
            containerColor = MaterialTheme.colorScheme.errorContainer,
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                        notificationPermissionDialogPresent = false
                    },
                    content = { Text(stringResource(R.string.open_settings)) }
                )
            },
            icon = { Icons.Filled.DeleteForever },
            title = { Text(stringResource(R.string.missing_notification_permission_title)) },
            text = { Text(stringResource(R.string.missing_notification_permission_description)) }
        )
    }

}

private fun Context.requestNotificationPermission(requester: ManagedActivityResultLauncher<String, Boolean>) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        when (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)) {
            PackageManager.PERMISSION_GRANTED -> {
                startTracker()
            }
            else -> requester.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    } else {
        startTracker()
    }
}

private fun Context.startTracker() {
    startActivity(
        Intent(
            this,
            TrackerActivity::class.java
        )
    )
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
