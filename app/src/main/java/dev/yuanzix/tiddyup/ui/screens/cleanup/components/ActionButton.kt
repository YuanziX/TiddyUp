package dev.yuanzix.tiddyup.ui.screens.cleanup.components


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    label: String,
    iconVector: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    return Box(
        modifier = modifier.then(Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick() }
            .background(backgroundColor)
            .padding(vertical = 16.dp, horizontal = 24.dp))
    ) {
        Row {
            Icon(
                imageVector = iconVector,
                contentDescription = label,
                tint = contentColor,
            )
            Text(
                text = label,
                color = contentColor,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}