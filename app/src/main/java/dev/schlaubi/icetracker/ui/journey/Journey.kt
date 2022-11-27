package dev.schlaubi.icetracker.ui.journey

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
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
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
import kotlin.io.path.copyTo
import kotlin.io.path.div
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
            Column(Modifier.padding(top = 3.dp, bottom = 10.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        name,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                            .fillMaxWidth(fraction = .9f)
                    )
                    Spacer(Modifier.weight(1f))
                    JourneyDropDown(journey, file, deleteAlertPresent, renameDialogPresent)
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
    file: Path,
    deleteAlertPresent: MutableState<Boolean>,
    renameDialogPresent: MutableState<Boolean>
) {
    var modifyExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column {
        IconButton(onClick = { modifyExpanded = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert, contentDescription = stringResource(
                    id = R.string.manage_journey
                )
            )
        }
        DropdownMenu(expanded = modifyExpanded, onDismissRequest = { modifyExpanded = false }) {
            DropdownMenuItem(text = { Text(stringResource(R.string.rename)) }, onClick = {
                modifyExpanded = false
                renameDialogPresent.value = true
            })
            DropdownMenuItem(text = { Text(stringResource(R.string.delete)) }, onClick = {
                modifyExpanded = false
                deleteAlertPresent.value = true
            })
            DropdownMenuItem(text = { Text(stringResource(R.string.convert_to_gpx)) }, onClick = {
                modifyExpanded = false
                context.convertToGpx(journey)
            })
            DropdownMenuItem(text = { Text(text = "Export") }, onClick = {
                modifyExpanded = false
                coroutineScope.launch(Dispatchers.IO) {
                    val tempFileToShare = file.copyTo(context.cacheDir.toPath() / "journey_to_export_${generateNonce()}.journey.json")
                    context.shareFile(tempFileToShare, R.string.export_journey, "application/json")
                }
            })
        }
    }
}


private fun Context.convertToGpx(journey: Journey) {
    val gpx = journey.toGPX(includeExtensions = false)
    val tempFile = Files.createTempFile("journey_", ".gpx")
    GPX.write(gpx, tempFile)

    shareFile(tempFile, R.string.convert_to_gpx, "application/gpx+xml")
}

private fun Context.shareFile(path: Path, @StringRes text: Int, type: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        this.type = type
        val file = FileProvider.getUriForFile(
            this@shareFile, applicationContext.packageName + ".provider", path.toFile()
        )

        putExtra(Intent.EXTRA_STREAM, file)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }


    startActivity(Intent.createChooser(shareIntent, resources.getString(text)))
}