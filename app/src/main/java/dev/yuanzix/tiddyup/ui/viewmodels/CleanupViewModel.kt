package dev.yuanzix.tiddyup.ui.viewmodels

import android.util.Log
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

    fun getMediaByAlbum(albumId: Long) {
        viewModelScope.launch {
            mediaFiles.value = emptyList()
            mediaReader.getImagesInAlbum(albumId)
                .catch { e ->
                    Log.e("CleanupViewModel", "Error fetching album files", e)
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
                    Log.e("CleanupViewModel", "Error fetching month files", e)
                }
                .collect { newMedia ->
                    mediaFiles.update { currentList ->
                        currentList + newMedia
                    }
                }
        }
    }
}