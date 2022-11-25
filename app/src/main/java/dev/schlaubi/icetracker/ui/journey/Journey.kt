package dev.schlaubi.icetracker.ui.journey

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import dev.schlaubi.icetracker.R
import dev.schlaubi.icetracker.fetcher.Journey
import dev.schlaubi.icetracker.fetcher.toGPX
import io.jenetics.jpx.GPX
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.io.path.outputStream


@OptIn(ExperimentalSerializationApi::class)
@Composable
fun JourneyCard(journey: Journey, file: Path, deleteFromList: (id: String) -> Unit) {
    var name by remember { mutableStateOf(journey.name) }
    val deleteAlertPresent = remember { mutableStateOf(false) }
    val renameDialogPresent = remember { mutableStateOf(false) }

    fun updateName(newName: String) {
        Json.encodeToStream(journey.copy(name = newName), file.outputStream())
        name = newName
    }

    ElevatedCard(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(vertical = 7.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        name,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(5.dp).fillMaxWidth(fraction = .9f)
                    )
                    Spacer(Modifier.weight(1f))
                    JourneyDropDown(journey, deleteAlertPresent, renameDialogPresent)
                }
                Row(
                    horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()
                ) {
                    JourneyDetail(
                        Icons.Filled.Train,
                        R.string.building_series,
                        journey.trainInfo.buildingSeries
                    )
                    JourneyDetail(
                        Icons.Filled.Route,
                        R.string.train_line,
                        "${journey.trainInfo.type} ${journey.number}"
                    )
                    val date = journey.createdAt.toLocalDateTime(TimeZone.UTC).toJavaLocalDateTime()
                    val formattedDate =
                        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(date)

                    JourneyDetail(
                        icon = Icons.Filled.Event,
                        description = R.string.journey_date,
                        text = formattedDate
                    )
                }
            }
        }

        DeleteJourneyDialog(deleteAlertPresent, file, deleteFromList, journey, name)
        RenameJourneyDialog(renameDialogPresent, name, ::updateName)
    }
}

@Composable
private fun JourneyDropDown(
    journey: Journey,
    deleteAlertPresent: MutableState<Boolean>,
    renameDialogPresent: MutableState<Boolean>
) {
    var modifyExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column {
        IconButton(onClick = { modifyExpanded = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert, contentDescription = stringResource(
                    id = R.string.manage_journey
                )
            )
        }
        DropdownMenu(expanded = modifyExpanded, onDismissRequest = { modifyExpanded = false }) {
            DropdownMenuItem(text = { Text(text = "Rename") }, onClick = {
                modifyExpanded = false
                renameDialogPresent.value = true
            })
            DropdownMenuItem(text = { Text(text = "Delete") }, onClick = {
                modifyExpanded = false
                deleteAlertPresent.value = true
            })
            DropdownMenuItem(text = { Text(text = "Generate GPX") }, onClick = {
                modifyExpanded = false
                context.convertToGpx(journey)
            })
        }
    }
}


private fun Context.convertToGpx(journey: Journey) {
    val gpx = journey.toGPX(includeExtensions = false)
    val tempFile = Files.createTempFile("journey_", ".gpx")
    GPX.write(gpx, tempFile)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/gpx+xml"
        val file = FileProvider.getUriForFile(
            this@convertToGpx, applicationContext.packageName + ".provider", tempFile.toFile()
        )

        putExtra(Intent.EXTRA_STREAM, file)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }


    startActivity(Intent.createChooser(shareIntent, resources.getString(R.string.convert_to_gpx)))
}