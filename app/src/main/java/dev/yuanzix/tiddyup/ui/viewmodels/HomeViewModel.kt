package dev.yuanzix.tiddyup.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.yuanzix.tiddyup.data.MediaHandler
import dev.yuanzix.tiddyup.models.Album
import dev.yuanzix.tiddyup.models.FilterCriteria
import dev.yuanzix.tiddyup.models.Month
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mediaReader: MediaHandler,
) : ViewModel() {
    var isFetched: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var hasImages: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val albums: MutableStateFlow<Set<Album>> = MutableStateFlow(emptySet())
    val months: MutableStateFlow<Set<Month>> = MutableStateFlow(emptySet())
    var selectedFilter: MutableStateFlow<FilterCriteria> = MutableStateFlow(FilterCriteria.NONE)
    var errorMessage: MutableStateFlow<String?> = MutableStateFlow(null)

    init {
        viewModelScope.launch {
            hasImages.value = mediaReader.haveImages()
            isFetched.value = true
        }
    }

    fun fetchAlbums() {
        viewModelScope.launch {
            mediaReader.getImageAlbums().catch { e ->
                errorMessage.value = "Failed to fetch albums"
            }.collect { album ->
                albums.value = albums.value.plus(album)
            }
        }
    }

    fun fetchMonths() {
        viewModelScope.launch {
            mediaReader.getMonthLabels().catch { e ->
                errorMessage.value = "Failed to fetch months"
            }.collect { month ->
                months.value = months.value.plus(month)
            }
        }
    }

    fun setFilterCriteria(fc: FilterCriteria) {
        selectedFilter.value = fc
    }

    fun resetErrorMessage() {
        errorMessage.value = null
    }
}
