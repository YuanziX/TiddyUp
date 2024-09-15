package dev.yuanzix.tiddyup.ui.screens.cleanup.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.yuanzix.tiddyup.models.MediaFile

@Composable
fun PreviewImage(
    index: Int,
    isActive: Boolean,
    mediaFile: MediaFile,
    onClick: (Int) -> Unit,
) {
    val scale = animateFloatAsState(if (isActive) 1f else 0.8f, label = "preview image $index")

    return Box(
        modifier = Modifier
            .size(100.dp)
            .padding(8.dp)
            .clickable { onClick(index) }
            .scale(scale.value)
    ) {
        AsyncImage(
            model = mediaFile.uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(if (isActive) CircleShape else RectangleShape),
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-8).dp, y = (-8).dp)
                .clip(CircleShape)
                .background(
                    color = if (mediaFile.toDelete) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                )
                .size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (mediaFile.toDelete) Icons.Default.Delete else Icons.Default.Check,
                contentDescription = "Check",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp)
            )
        }


    }
}