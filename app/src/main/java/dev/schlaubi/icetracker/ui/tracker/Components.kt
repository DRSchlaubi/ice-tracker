package dev.schlaubi.icetracker.ui.tracker

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun Station(station: String, description: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(0.dp, 150.dp)
    ) {
        Text(text = station, style = MaterialTheme.typography.displaySmall)
        Text(text = description, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun RowButton(onClick: () -> Unit, enabled: Boolean = true, content: @Composable RowScope.() -> Unit) {
    Button(
        onClick, enabled = enabled, modifier = Modifier
            .fillMaxWidth()
            .height(50.dp), content = content
    )
}
