package dev.yuanzix.tiddyup.ui.screens.cleanup

import CurrentImage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.yuanzix.tiddyup.models.FilterCriteria
import dev.yuanzix.tiddyup.ui.screens.cleanup.components.PreviewImage
import dev.yuanzix.tiddyup.ui.viewmodels.CleanupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanupScreen(
    viewModel: CleanupViewModel = hiltViewModel(),
    filterCriteria: FilterCriteria,
    albumId: Long,
    month: String?,
) {
    val mediaFiles by viewModel.mediaFiles.collectAsState()
    val index by viewModel.currentIndex.collectAsState(0)
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(filterCriteria, albumId, month) {
        when (filterCriteria) {
            FilterCriteria.ALBUM -> viewModel.getMediaByAlbum(albumId)
            FilterCriteria.MONTH -> month?.let { viewModel.getMediaByMonth(it) }
            else -> {}
        }
    }

    if (isLoading && mediaFiles.isNotEmpty()) {
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    text = month ?: "Album: $albumId", style = MaterialTheme.typography.titleLarge
                )
            })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            } else {
                LazyRow {
                    itemsIndexed(mediaFiles) { ind, mediaFile ->
                        PreviewImage(
                            index = ind,
                            isActive = ind == index,
                            mediaFile = mediaFile,
                            onClick = { viewModel.setIndexTo(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.size(24.dp))

                CurrentImage(imageUri = mediaFiles[index].uri, onRightSwipe = {
                    viewModel.keepAndNext()
                }, onLeftSwipe = {
                    viewModel.deleteAndNext()
                })

                Spacer(modifier = Modifier.size(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { viewModel.deleteAndNext() }
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(vertical = 16.dp, horizontal = 24.dp)
                    ) {
                        Row {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            Text(
                                text = "Delete",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                    Box(
                        Modifier
                            .clickable { viewModel.keepAndNext() }
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(vertical = 16.dp, horizontal = 24.dp)
                    ) {
                        Row {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Keep",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = "Keep",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }

            }
        }
    }
}
