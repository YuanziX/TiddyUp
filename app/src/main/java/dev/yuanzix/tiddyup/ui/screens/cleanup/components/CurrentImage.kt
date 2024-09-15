package dev.yuanzix.tiddyup.ui.screens.cleanup.components

import android.net.Uri
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun CurrentImage(imageUri: Uri) {
    val config = LocalConfiguration.current

    return AsyncImage(
        model = imageUri,
        contentDescription = null,
        modifier = Modifier.size(config.screenWidthDp.dp * 0.8f, config.screenHeightDp.dp * 0.8f)
    )
}