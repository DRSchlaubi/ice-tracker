package dev.schlaubi.icetracker.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.MemoryFile
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import dev.schlaubi.icetracker.R
import dev.schlaubi.icetracker.fetcher.Journey
import dev.schlaubi.icetracker.fetcher.toGPX
import io.jenetics.jpx.GPX
import io.ktor.util.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.nio.file.Files
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.io.path.Path
import kotlin.io.path.div


@Composable
fun JourneyCard(journey: Journey) {
    var modifyExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
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
                        journey.name,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(5.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    Column {
                        IconButton(onClick = { modifyExpanded = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = stringResource(
                                    id = R.string.manage_journey
                                )
                            )
                        }
                        DropdownMenu(
                            expanded = modifyExpanded,
                            onDismissRequest = { modifyExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text(text = "Rename") },
                                onClick = { /*TODO*/ })
                            DropdownMenuItem(
                                text = { Text(text = "Delete") },
                                onClick = { /*TODO*/ })
                            DropdownMenuItem(text = { Text(text = "Generate GPX") }, onClick = {
                                context.convertToGpx(journey)
                            })
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
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
                    val date = journey.createdAt.toLocalDateTime(TimeZone.UTC)
                        .toJavaLocalDateTime()
                    val formattedDate =
                        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                            .format(date)

                    JourneyDetail(
                        icon = Icons.Filled.Event,
                        description = R.string.journey_date,
                        text = formattedDate
                    )
                }
            }
        }
    }
}

@Composable
private fun JourneyDetail(icon: ImageVector, @StringRes description: Int, text: String) {
    Row(Modifier.padding(horizontal = 3.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(
                id = description
            )
        )
        Spacer(modifier = Modifier.padding(horizontal = 3.dp))
        Text(text)
    }
}

private fun Context.convertToGpx(journey: Journey) {
    val gpx = journey.toGPX(includeExtensions = false)
    val tempFile = Files.createTempFile("journey_", ".gpx")
    GPX.write(gpx, tempFile)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/gpx+xml"
        val file = FileProvider.getUriForFile(
            this@convertToGpx,
            applicationContext.packageName + ".provider",
            tempFile.toFile()
        )

        putExtra(Intent.EXTRA_STREAM, file)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }


    startActivity(Intent.createChooser(shareIntent, resources.getString(R.string.convert_to_gpx)))
}