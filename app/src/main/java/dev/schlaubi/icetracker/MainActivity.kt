package dev.schlaubi.icetracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import dev.schlaubi.icetracker.fetcher.Journey
import dev.schlaubi.icetracker.service.TrackerService
import dev.schlaubi.icetracker.service.TrackingServiceState
import dev.schlaubi.icetracker.ui.journey.JourneyList
import dev.schlaubi.icetracker.ui.theme.ICETrackerTheme
import dev.schlaubi.icetracker.ui.tracker.STARTED_FROM_MAIN_SCREEN
import dev.schlaubi.icetracker.ui.tracker.TrackerActivity
import kotlinx.datetime.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.nio.file.StandardOpenOption
import kotlin.io.path.*

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
        ).apply {
            putExtra(STARTED_FROM_MAIN_SCREEN, true)
        }
    )
}

@SuppressLint("BatteryLife") // unavoidable
@Composable
private fun ActionDropDown() {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var askForBatteryPermission by remember { mutableStateOf(!isIgnoringPowerOptimizations(context)) }
    val fileRequester = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        val uri = it ?: return@rememberLauncherForActivityResult
        context.contentResolver.openInputStream(uri)!!.use { stream ->
            val bytes = stream.readBytes()
            val parsed = Json.decodeFromString<Journey>(bytes.toString(Charsets.UTF_8))

            val file = context.filesDir.toPath() / "journeys" / "journey_${parsed.id}.journey.json"
            if (!file.parent.exists()) {
                file.parent.createDirectories()
            }
            file.writeBytes(bytes, StandardOpenOption.CREATE)

        }

        Toast.makeText(context, "Will be available after Restart", Toast.LENGTH_SHORT).show()
    }

    IconButton(onClick = { expanded = true }, modifier = Modifier.wrapContentSize()) {
        if (askForBatteryPermission) {
            Box(
                Modifier
                    .size(6.dp)
                    .offset(8.dp, (-13).dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
        }
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(R.string.options)
        )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(text = { Text(stringResource(R.string.import_item)) }, onClick = {
            fileRequester.launch("application/json")
            expanded = false
        })

        DropdownMenuItem(text = { Text(stringResource(R.string.open_source_licenses)) }, onClick = {
            expanded = false

            context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
        })

        if (askForBatteryPermission) {
            DropdownMenuItem(
                text = {
                    Box(
                        Modifier
                            .size(6.dp)
                            .offset(183.dp, (-3).dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                    Text(stringResource(R.string.request_battery_permission))
                },
                onClick = {
                    val permissionIntent =
                        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                        }
                    context.startActivity(permissionIntent)
                    expanded = false; askForBatteryPermission = false
                })
        }
    }
}


private fun isIgnoringPowerOptimizations(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    return powerManager.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)
}
