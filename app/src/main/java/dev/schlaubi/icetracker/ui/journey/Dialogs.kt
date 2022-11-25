package dev.schlaubi.icetracker.ui.journey

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.schlaubi.icetracker.R
import dev.schlaubi.icetracker.fetcher.Journey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.file.Path
import kotlin.io.path.deleteIfExists

@Composable
fun DeleteJourneyDialog(
    deleteAlertPresent: MutableState<Boolean>,
    file: Path,
    deleteFromList: (id: String) -> Unit,
    journey: Journey,
    name: String
) {
    JourneyDialog(
        present = deleteAlertPresent, onSubmit = {
            file.deleteIfExists()
            deleteFromList(journey.id)
        }, backgroundColor = MaterialTheme.colorScheme.errorContainer,
        title = { Text(stringResource(R.string.delete_title)) },
        text = { Text(stringResource(R.string.delete_description, name)) },
        confirmButtonContent = {
            Icon(
                imageVector = Icons.Filled.DeleteForever,
                contentDescription = stringResource(R.string.delete)
            )
            Text(stringResource(R.string.delete))
        },
        confirmButtonColors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameJourneyDialog(
    renameDialogPresent: MutableState<Boolean>,
    name: String,
    changeName: (newName: String) -> Unit
) {
    var newName by remember { mutableStateOf(name) }
    JourneyDialog(
        present = renameDialogPresent,
        onSubmit = { changeName(newName) },
        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
        title = { Text(stringResource(R.string.rename_journey_title)) },
        text = {
            Column {
                Text(stringResource(R.string.rename_journey_description, name))
                Spacer(Modifier.padding(vertical = 5.dp))
                OutlinedTextField(value = newName, onValueChange = { newName = it }, label = {
                    Text(
                        stringResource(id = R.string.new_name)
                    )
                })
            }
        },
        confirmButtonContent = {
            Icon(
                imageVector = Icons.Filled.DriveFileRenameOutline,
                contentDescription = stringResource(R.string.rename_journey)
            )
            Text(stringResource(R.string.rename_journey))
        },
        confirmButtonColors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
        enabled = newName != name && newName.isNotEmpty(), { newName = name } // reset name state
    )
}

@Composable
private fun JourneyDialog(
    present: MutableState<Boolean>,
    onSubmit: suspend CoroutineScope.() -> Unit,
    backgroundColor: Color,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    confirmButtonContent: @Composable RowScope.() -> Unit,
    confirmButtonColors: ButtonColors = ButtonDefaults.buttonColors(),
    enabled: Boolean = true,
    onDismiss: (() -> Unit)? = null
) {
    if (present.value) {
        var loading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        AlertDialog(
            onDismissRequest = { present.value = false; onDismiss?.invoke() },
            containerColor = backgroundColor,
            confirmButton = {
                if (loading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            loading = true
                            scope.launch(Dispatchers.IO) {
                                onSubmit()
                                loading = false
                                present.value = false
                            }
                        },
                        colors = confirmButtonColors,
                        content = confirmButtonContent,
                        enabled = enabled
                    )
                }
            },
            dismissButton = {
                Button(onClick = { present.value = false; onDismiss?.invoke() }, enabled = !loading) {
                    Text(stringResource(R.string.abort))
                }
            },
            icon = { Icons.Filled.DeleteForever },
            title = title,
            text = text
        )
    }
}
