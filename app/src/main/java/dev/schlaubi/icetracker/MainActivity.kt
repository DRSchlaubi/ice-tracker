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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import dev.schlaubi.icetracker.fetcher.Journey
import dev.schlaubi.icetracker.ui.JourneyList
import dev.schlaubi.icetracker.ui.theme.ICETrackerTheme
import kotlinx.datetime.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.setProperty(
            "android.javax.xml.stream.XMLInputFactory",
            "com.sun.xml.stream.ZephyrParserFactory"
        );
        System.setProperty(
            "android.javax.xml.stream.XMLOutputFactory",
            "com.sun.xml.stream.ZephyrWriterFactory"
        );
        System.setProperty(
            "android.javax.xml.stream.XMLEventFactory",
            "com.sun.xml.stream.events.ZephyrEventFactory"
        );
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
