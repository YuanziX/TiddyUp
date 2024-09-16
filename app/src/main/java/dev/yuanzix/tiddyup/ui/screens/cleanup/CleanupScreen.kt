package dev.yuanzix.tiddyup.ui.screens.cleanup

import CurrentImage
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.yuanzix.tiddyup.models.FilterCriteria
import dev.yuanzix.tiddyup.models.MediaFile
import dev.yuanzix.tiddyup.ui.screens.cleanup.components.ActionButton
import dev.yuanzix.tiddyup.ui.screens.cleanup.components.ConfirmDeletionModalSheet
import dev.yuanzix.tiddyup.ui.screens.cleanup.components.PreviewImage
import dev.yuanzix.tiddyup.ui.viewmodels.CleanupViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CleanupScreen(
    modifier: Modifier,
    viewModel: CleanupViewModel = hiltViewModel(),
    filterCriteria: FilterCriteria,
    albumId: Long,
    albumName: String?,
    month: String?,
    goBack: () -> Boolean,
) {
    val mediaFiles by viewModel.mediaFiles.collectAsState()
    val index by viewModel.currentIndex.collectAsState(0)
    var isLoading by remember { mutableStateOf(true) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) {
        if (it.resultCode == RESULT_OK) goBack()
        if (it.resultCode == RESULT_CANCELED) viewModel.setErrorMessage("Delete operation cancelled")
        else viewModel.setErrorMessage("Error deleting files")
    }

    val showBottomSheet by viewModel.showBottomSheet.collectAsState(false)
    val sheetState = rememberModalBottomSheetState()

    val errorMessage by viewModel.errorMessage.collectAsState(null)
    val snackbarHostState = remember { SnackbarHostState() }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                errorMessage.toString(), duration = SnackbarDuration.Short
            )
            viewModel.resetErrorMessage()
        }
    }

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
                    text = month ?: albumName ?: "Album: $albumId",
                    style = MaterialTheme.typography.titleLarge
                )
            }, navigationIcon = {
                IconButton(onClick = { goBack() }, content = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back"
                    )
                })
            })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = modifier.then(
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .scrollable(
                        state = rememberScrollState(),
                        orientation = Orientation.Vertical,
                    )
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            } else {
                if (showBottomSheet && !mediaFiles.none(MediaFile::toDelete)) {
                    ConfirmDeletionModalSheet(
                        mediaFiles = mediaFiles.filter { it.toDelete },
                        onConfirm = {
                            viewModel.hideBottomSheet()
                            viewModel.deleteMarked(launcher)
                        },
                        onDismissRequest = { viewModel.hideBottomSheet() },
                        onToggleDelete = { imageUri ->
                            viewModel.toggleDeleteFlag(imageUri)
                        },
                        sheetState = sheetState,
                    )
                }

                LazyRow(
                    state = listState,
                ) {
                    itemsIndexed(mediaFiles) { ind, mediaFile ->
                        PreviewImage(index = ind,
                            isActive = ind == index,
                            mediaFile = mediaFile,
                            onClick = { viewModel.setIndexTo(it) })
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                CurrentImage(imageUri = mediaFiles[index].uri, onRightSwipe = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(index = index)
                    }
                    viewModel.keepAndNext()
                }, onLeftSwipe = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(index = index)
                    }
                    viewModel.deleteAndNext()
                })

                Spacer(modifier = Modifier.weight(1f))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = spacedBy(
                        space = 12.dp,
                        alignment = Alignment.CenterHorizontally,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ActionButton(
                        label = "Delete",
                        iconVector = Icons.Default.Delete,
                        backgroundColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ) {
                        coroutineScope.launch {
                            listState.animateScrollToItem(index = index)
                        }
                        viewModel.deleteAndNext()
                    }
                    ActionButton(
                        label = "Keep",
                        iconVector = Icons.Default.Check,
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ) {
                        coroutineScope.launch {
                            listState.animateScrollToItem(index = index)
                        }
                        viewModel.keepAndNext()
                    }
                    ActionButton(
                        label = "Review Deletions",
                        iconVector = Icons.Default.PlayArrow,
                        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    ) { viewModel.showBottomSheet() }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
