package dev.yuanzix.tiddyup.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import dev.yuanzix.tiddyup.R
import dev.yuanzix.tiddyup.models.FilterCriteria
import dev.yuanzix.tiddyup.ui.screens.home.components.AlbumGrouping
import dev.yuanzix.tiddyup.ui.screens.home.components.FilterChips
import dev.yuanzix.tiddyup.ui.screens.home.components.MonthGrouping
import dev.yuanzix.tiddyup.ui.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToCleanup: (filterCriteria: FilterCriteria, albumId: Long, month: String?) -> Unit,
) {
    val initialFetched by viewModel.isFetched.collectAsState(false)
    val isNotEmpty by viewModel.hasImages.collectAsState(false)
    val isLoading by viewModel.isLoading.collectAsState(false)
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.cleaning))
    val selectedFilter by viewModel.selectedFilter.collectAsState(FilterCriteria.NONE)

    if (!initialFetched || isLoading) {
        return Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
        }
    }

    if (!isNotEmpty) {
        return Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "No media files found",
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                "Dang, that's surprising\n(or you have circumvented the permission check somehow)",
                textAlign = TextAlign.Center
            )
        }
    }

    viewModel.fetchAlbums()

    return Scaffold(topBar = {
        TopAppBar(
            title = { Text("Hi there") },
        )
    }) { paddingValues ->
        Column(
            modifier = modifier.then(
                Modifier.padding(paddingValues),
            ), verticalArrangement = Arrangement.Center
        ) {
            FilterChips(selected = selectedFilter) { filterCriteria ->
                viewModel.setFilterCriteria(filterCriteria)
            }

            AnimatedContent(
                selectedFilter, label = "filtering hard or hardly filtering"
            ) { targetState ->
                when (targetState) {
                    FilterCriteria.ALBUM -> {
                        AlbumGrouping(
                            viewModel = viewModel,
                            onNavigateToCleanup = onNavigateToCleanup
                        )
                    }

                    FilterCriteria.MONTH -> {
                        MonthGrouping(
                            viewModel = viewModel,
                            onNavigateToCleanup = onNavigateToCleanup
                        )
                    }

                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            LottieAnimation(
                                composition,
                                iterations = Integer.MAX_VALUE,
                                modifier = Modifier.size(400.dp)
                            )
                            Text(
                                "Select a filter to get started",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }
        }
    }
}