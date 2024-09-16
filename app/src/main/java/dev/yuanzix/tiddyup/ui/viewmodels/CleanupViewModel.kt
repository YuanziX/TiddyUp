package dev.yuanzix.tiddyup.ui.viewmodels

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.yuanzix.tiddyup.data.MediaHandler
import dev.yuanzix.tiddyup.models.MediaFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CleanupViewModel @Inject constructor(
    private val mediaReader: MediaHandler,
) : ViewModel() {
    val mediaFiles: MutableStateFlow<List<MediaFile>> = MutableStateFlow(emptyList())
    val currentIndex: MutableStateFlow<Int> = MutableStateFlow(0)
    val showBottomSheet: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val errorMessage: MutableStateFlow<String?> = MutableStateFlow(null)

    fun getMediaByAlbum(albumId: Long) {
        viewModelScope.launch {
            mediaFiles.value = emptyList()
            mediaReader.getImagesInAlbum(albumId)
                .catch { e ->
                    errorMessage.value = "Error fetching album files"
                }
                .collect { newMedia ->
                    mediaFiles.update { currentList ->
                        currentList + newMedia
                    }
                }
        }
    }

    fun getMediaByMonth(month: String) {
        viewModelScope.launch {
            mediaFiles.value = emptyList()
            mediaReader.getImagesForMonth(month)
                .catch { e ->
                    errorMessage.value = "Error fetching month files"
                }
                .collect { newMedia ->
                    mediaFiles.update { currentList ->
                        currentList + newMedia
                    }
                }
        }
    }

    private fun next() {
        if (currentIndex.value < mediaFiles.value.size - 1) {
            currentIndex.value++
        } else {
            showBottomSheet.value = true
        }
    }

    fun hideBottomSheet() {
        showBottomSheet.value = false
    }

    fun showBottomSheet() {
        showBottomSheet.value = true
    }

    fun setIndexTo(index: Int) {
        if (index < mediaFiles.value.size) {
            currentIndex.value = index
        }
    }

    private fun setBoolAndNext(bool: Boolean) {
        viewModelScope.launch {
            val currentIndex = currentIndex.value
            if (currentIndex in mediaFiles.value.indices) {
                val updatedFile = mediaFiles.value[currentIndex].copy(toDelete = bool)
                mediaFiles.value = mediaFiles.value.toMutableList().apply {
                    set(currentIndex, updatedFile)
                }
            }
            next()
        }
    }

    fun keepAndNext() {
        // flag - toDelete
        setBoolAndNext(false)
    }

    fun deleteAndNext() {
        // flag - toDelete
        setBoolAndNext(true)
    }

    fun deleteMarked(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        viewModelScope.launch {
            mediaReader.createDeleteRequest(mediaFiles.value.filter { it.toDelete }, launcher)
        }
    }

    fun toggleDeleteFlag(mediaUri: Uri) {
        viewModelScope.launch {
            val index = mediaFiles.value.indexOfFirst { it.uri == mediaUri }
            if (index != -1) {
                val updatedFile =
                    mediaFiles.value[index].copy(toDelete = !mediaFiles.value[index].toDelete)
                mediaFiles.value = mediaFiles.value.toMutableList().apply {
                    set(index, updatedFile)
                }
            }
        }
    }

    fun setErrorMessage(message: String) {
        errorMessage.value = message
    }

    fun resetErrorMessage() {
        errorMessage.value = null
    }
}