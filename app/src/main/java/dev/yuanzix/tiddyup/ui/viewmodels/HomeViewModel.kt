package dev.yuanzix.tiddyup.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.yuanzix.tiddyup.data.MediaHandler
import dev.yuanzix.tiddyup.models.Album
import dev.yuanzix.tiddyup.models.FilterCriteria
import dev.yuanzix.tiddyup.models.Month
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mediaReader: MediaHandler,
) : ViewModel() {
    var isFetched: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var hasImages: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val albums: MutableStateFlow<Set<Album>> = MutableStateFlow(emptySet())
    val months: MutableStateFlow<Set<Month>> = MutableStateFlow(emptySet())
    var selectedFilter: MutableStateFlow<FilterCriteria> = MutableStateFlow(FilterCriteria.NONE)

    init {
        viewModelScope.launch {
            hasImages.value = mediaReader.haveImages()
            isFetched.value = true
        }
    }

    fun fetchAlbums() {
        isLoading.value = true
        viewModelScope.launch {
            mediaReader.getImageAlbums().catch { e ->
                Log.e("MainViewModel", "Error fetching albums", e)
            }.onCompletion {
                isLoading.value = false
            }.collect { album ->
                albums.value = albums.value.plus(album)
            }
        }
    }

    fun fetchMonths() {
        isLoading.value = true
        viewModelScope.launch {
            mediaReader.getMonthLabels().catch { e ->
                Log.e("MainViewModel", "Error fetching months", e)
            }.onCompletion {
                isLoading.value = false
            }.collect { month ->
                months.value = months.value.plus(month)
            }
        }
    }

    fun setFilterCriteria(fc: FilterCriteria) {
        isLoading.value = true
        selectedFilter.value = fc
        isLoading.value = false
    }
}