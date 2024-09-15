package dev.yuanzix.tiddyup.ui.screens.cleanup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import dev.yuanzix.tiddyup.models.FilterCriteria
import dev.yuanzix.tiddyup.ui.screens.cleanup.components.CurrentImage
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
        isLoading = true
        when (filterCriteria) {
            FilterCriteria.ALBUM -> viewModel.getMediaByAlbum(albumId)
            FilterCriteria.MONTH -> month?.let { viewModel.getMediaByMonth(it) }
            else -> {}
        }
    }

    LaunchedEffect(mediaFiles) {
        if (mediaFiles.isNotEmpty()) {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    (month ?: "Album: $albumId").toString()
                )
            })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else if (mediaFiles.isNotEmpty()) {
                CurrentImage(imageUri = mediaFiles[index].uri)
            } else {
                Text(
                    "No media files found",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}