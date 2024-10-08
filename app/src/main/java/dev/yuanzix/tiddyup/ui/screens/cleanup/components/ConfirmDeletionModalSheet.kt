package dev.yuanzix.tiddyup.ui.screens.cleanup.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.yuanzix.tiddyup.models.MediaFile

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ConfirmDeletionModalSheet(
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    onToggleDelete: (Uri) -> Unit,
    mediaFiles: List<MediaFile>,
) {
    var filesToShow by remember { mutableStateOf(mediaFiles) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Review Deletions",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ContextualFlowRow(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.SpaceEvenly,
                itemCount = filesToShow.size
            ) { index ->
                ConfirmDeletionPreviewImage(
                    mediaFile = filesToShow[index],
                    onClick = {
                        // update local state
                        val updatedFile =
                            filesToShow[index].copy(toDelete = !filesToShow[index].toDelete)
                        val updatedFiles = filesToShow.toMutableList().apply {
                            set(index, updatedFile)
                        }
                        filesToShow = updatedFiles

                        // update viewmodel state
                        onToggleDelete(updatedFile.uri)
                    }
                )
            }

            ActionButton(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.End),
                label = "Confirm Deletions",
                iconVector = Icons.Default.PlayArrow,
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = onConfirm,
            )
        }
    }
}
