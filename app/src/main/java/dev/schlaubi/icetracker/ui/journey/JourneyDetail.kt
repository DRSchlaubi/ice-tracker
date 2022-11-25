package dev.schlaubi.icetracker.ui.journey

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun JourneyDetail(icon: ImageVector, @StringRes description: Int, text: String) {
    Row(Modifier.padding(horizontal = 3.dp)) {
        Icon(
            imageVector = icon, contentDescription = stringResource(
                id = description
            )
        )
        Spacer(modifier = Modifier.padding(horizontal = 3.dp))
        Text(text)
    }
}
